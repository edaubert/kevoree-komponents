package org.kevoree.library.javase.http.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.ModelService;
import org.kevoree.library.javase.http.netty.NettyServerHandler;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/14
 * Time: 08:37
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyDataHandler extends NettyServerHandler {
    private ModelService modelService;
    private BootstrapService bootstrapService;
    private String nodeName;

    private AbstractNettyChannel channel;

    private ObjectDecoderImpl decoder;

    public NettyDataHandler(ModelService modelService, BootstrapService bootstrapService, String nodeName, AbstractNettyChannel channel) {
        this.modelService = modelService;
        this.bootstrapService = bootstrapService;
        this.nodeName = nodeName;
        this.channel = channel;
        decoder = new ObjectDecoderImpl(new ChannelClassResolver(modelService, bootstrapService, nodeName, channel));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        Object content = decoder.decode(channelHandlerContext, fullHttpRequest.content());

//        byte[] bytes = Reader.readContent(fullHttpRequest.content());


    }

    class ObjectDecoderImpl extends ObjectDecoder {

        public ObjectDecoderImpl(ClassResolver classResolver) {
            super(classResolver);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            return super.decode(ctx, in);
        }
    }
}
