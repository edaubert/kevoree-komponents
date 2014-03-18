package org.kevoree.library.javase.http.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kevoree.library.javase.http.netty.helpers.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/03/14
 * Time: 12:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ChannelHandler.Sharable
public abstract class NettyClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private NettyClientOutput output;

    public synchronized NettyClientOutput getOutput() {
        return output;
    }

    @Override
    protected synchronized void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {
        output = new NettyClientOutput();
        output.setResponseCode(fullHttpResponse.getStatus().code());

        output.setContent(new ByteArrayInputStream(Reader.readContent(fullHttpResponse.content())));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }


    protected abstract void processRequest(String host, int port, String uri, InputStream content, FullHttpRequest request) throws IOException;
}
