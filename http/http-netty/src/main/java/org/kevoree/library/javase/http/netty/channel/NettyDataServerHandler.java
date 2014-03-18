package org.kevoree.library.javase.http.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.ModelService;
import org.kevoree.library.javase.http.netty.NettyServerHandler;
import org.kevoree.library.javase.http.netty.helpers.Reader;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/14
 * Time: 08:37
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ChannelHandler.Sharable
public class NettyDataServerHandler extends NettyServerHandler {
    private ModelService modelService;
    private BootstrapService bootstrapService;
    private String nodeName;

    private AbstractNettyChannel channel;

    private DataCodec codec;

    public NettyDataServerHandler(ModelService modelService, BootstrapService bootstrapService, String nodeName, AbstractNettyChannel channel) {
        this.modelService = modelService;
        this.bootstrapService = bootstrapService;
        this.nodeName = nodeName;
        this.channel = channel;
        codec = new DataCodec(modelService, bootstrapService, nodeName, channel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        ByteBuf buf = fullHttpRequest.content();

        System.err.println(new String(Reader.readContent(buf)));

        Object content = codec.decode(channelHandlerContext, buf);
        Object result = channel.dispatchLocal(content);
        buf = Unpooled.buffer();
        try {
            codec.encode(result, buf);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }


        byte[] bytes = Reader.readContent(buf);

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK);
        httpResponse.content().writeBytes(bytes);

        httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
        channelHandlerContext.channel().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
