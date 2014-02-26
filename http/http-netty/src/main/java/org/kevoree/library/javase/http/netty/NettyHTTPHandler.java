package org.kevoree.library.javase.http.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.kevoree.library.javase.http.api.commons.HTTPOperationTuple;
import org.kevoree.library.javase.http.api.commons.Monitor;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletRequest;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletResponse;
import org.kevoree.log.Log;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/08/13
 * Time: 16:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ChannelHandler.Sharable
public class NettyHTTPHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private NettyHTTPServer server;

    public NettyHTTPHandler(NettyHTTPServer server) {
        this.server = server;

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
            KevoreeHTTPServletResponse response = new NettyKevoteeHTTPServletResponse(ctx, httpResponse);
            Monitor monitor = new Monitor(server.getTimeout(), server);
            HTTPOperationTuple result = monitor.request(new HTTPOperationTuple(request, response, monitor));
            Log.info("Status of the response: {} for request uri: {}", httpResponse.getStatus(), request.getRequestURI());

            ((NettyKevoteeHTTPServletResponse)response).end();

            if (httpResponse.headers().get(CONTENT_LENGTH) == null) {
                httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
            }

            if (!isKeepAlive(httpRequest)) {
                // Close the connection when the whole content is written out.
                ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
            } else {
                httpResponse.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(httpResponse);
            }
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
            ctx.close();
        }
    }

    void response(HTTPOperationTuple param) {
        // use the response
        param.monitor.response(param);
    }
}
