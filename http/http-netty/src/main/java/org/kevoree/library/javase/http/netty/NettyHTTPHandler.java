package org.kevoree.library.javase.http.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.kevoree.library.javase.http.api.HTTPOperationTuple;
import org.kevoree.library.javase.http.api.KevoreeHTTPServletRequest;
import org.kevoree.library.javase.http.api.KevoreeHTTPServletResponse;
import org.kevoree.log.Log;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/08/13
 * Time: 16:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyHTTPHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private NettyHTTPServer server;
    private Monitor monitor;

    public NettyHTTPHandler(NettyHTTPServer server) {
        this.server = server;
        monitor = new Monitor(Long.parseLong(server.getDictionary().get("timeout").toString()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        Log.debug("New request to handle: {}", httpRequest.getUri());

        if (!httpRequest.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
        } else {
            // transform httpRequest in an adequate type, send it through the monitor
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK);

            KevoreeHTTPServletRequest request = new NettyKevoreeHTTPServletRequest(httpRequest, server);
            KevoreeHTTPServletResponse response = new NettyKevoteeHTTPServletResponse(httpResponse);
            HTTPOperationTuple result = monitor.request(new HTTPOperationTuple(request, response));
            Log.info("Status of the response: {} for request uri: {}", httpResponse.getStatus(), request.getRequestURI());

        /*if (httpResponse.getStatus() < 200 || (httpResponse.getStatus() >= 300 && httpResponse.getStatus() < 500)) {
            if (server.isPortBinded("error")) {
                monitor.error(result);
            } else {
                Log.info("There is no management of client error status code");
            }
        }*/
            ctx.write(httpResponse);
            ctx.flush();
        }
        Log.debug("End of handler for {}", httpRequest.getUri());
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    void response(HTTPOperationTuple param) {
        // use the response
        monitor.response(param);
    }

    class Monitor {
        private long timeout;
        private KevoreeHTTPServletRequest request;
        private KevoreeHTTPServletResponse response;

        Monitor(long timeout) {
            this.timeout = timeout;
        }

        synchronized HTTPOperationTuple request(HTTPOperationTuple param) throws InterruptedException {
            response = null;
            request = param.request;
            server.request(param);
            wait(timeout);
            if (response == null) {
                param.response.setStatus(408);
            } else {
                param.response = response;
            }
            return param;
        }

        synchronized void response(HTTPOperationTuple param) {
            if (param.request == request) {
                response = param.response;
                notify();
            } else {
                Log.warn("timeout exceeds for request uri: {}", param.request.getRequestURI());
            }
        }

        /*synchronized HTTPOperationTuple error(HTTPOperationTuple param) throws InterruptedException {
            response = null;
            server.error(param);
            wait(timeout);
            if (response == null) {
                param.response.setStatus(404);
            } else {
                param.response = response;
            }
            return param;

        }*/

    }
}
