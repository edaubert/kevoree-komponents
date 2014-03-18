package org.kevoree.library.javase.http.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.ModelService;
import org.kevoree.log.Log;

import java.io.Serializable;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/03/14
 * Time: 16:05
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class DataCodec {
    private ObjectDecoderImpl decoder;
    private ObjectEncoderImpl encoder;

    private AbstractNettyChannel channel;

    public DataCodec(ModelService modelService, BootstrapService bootstrapService, String nodeName, AbstractNettyChannel channel) {
        decoder = new ObjectDecoderImpl(new ChannelClassResolver(modelService, bootstrapService, nodeName, channel));
        encoder = new ObjectEncoderImpl();
        this.channel = channel;
    }

    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object content = null;
        int type = in.readByte();
        if (type == 1) {
            byte[] bytes = new byte[in.readableBytes() - 1];
            in.readBytes(bytes, 0, bytes.length);
            content = new String(bytes, "UTF-8");
        } else if (type == 0) {
            content = decoder.decode(ctx, in.copy(1, in.readableBytes() -1));
        } else {
            Log.warn("Unable to decode the receive data");
        }
        return content;
    }

    public void encode(Object msg, ByteBuf out) throws Exception {
        if (msg != null) {
            if (msg instanceof String) {
                out.writeByte(1);
                out.writeBytes(((String) msg).getBytes());
            } else if (msg instanceof Serializable) {
                out.writeByte(0);
                encoder.encode(null, (Serializable) msg, out);
            } else {
                throw new Exception("Unable to manage payload from type " + msg.getClass() + " on " + channel.getInstanceName());
            }
        }
    }

    private class ObjectDecoderImpl extends ObjectDecoder {

        public ObjectDecoderImpl(ClassResolver classResolver) {
            super(classResolver);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            return super.decode(ctx, in);
        }
    }

    private class ObjectEncoderImpl extends ObjectEncoder {
        @Override
        protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }
}
