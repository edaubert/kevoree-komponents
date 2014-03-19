package org.kevoree.library.javase.http.netty.component;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.kevoree.library.javase.http.api.commons.HTTPOperationTuple;
import org.kevoree.library.javase.http.api.commons.Monitor;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletRequest;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletResponse;
import org.kevoree.library.javase.http.netty.NettyServerHandler;
import org.kevoree.log.Log;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
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
@ChannelHandler.Sharable
public class NettyHTTPHandler extends NettyServerHandler {
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
            Log.debug("Status of the response: {} for request uri: {}", httpResponse.getStatus(), request.getRequestURI());

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

    void response(HTTPOperationTuple param) {
        // use the response
        param.monitor.response(param);
    }
}
