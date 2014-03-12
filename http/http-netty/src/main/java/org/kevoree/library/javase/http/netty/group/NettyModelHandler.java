package org.kevoree.library.javase.http.netty.group;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.kevoree.ContainerRoot;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.library.javase.http.netty.NettyHandler;
import org.kevoree.library.javase.http.netty.helpers.Reader;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/03/14
 * Time: 18:06
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyModelHandler extends NettyHandler {

    private ModelService modelService;
    private JSONModelSerializer serializer;
    private JSONModelLoader loader;

    private AbstractNettyHttpGroup group;

    private long timeout;

    public NettyModelHandler(AbstractNettyHttpGroup group, ModelService modelService, long timeout) {
        this.group = group;
        this.modelService = modelService;
        this.timeout = timeout;
        serializer = new JSONModelSerializer();
        loader = new JSONModelLoader();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if (!fullHttpRequest.getDecoderResult().isSuccess()) {
            sendError(channelHandlerContext, HttpResponseStatus.BAD_REQUEST);
        } else {
            if (fullHttpRequest.getUri().equalsIgnoreCase("/pull")
                    || fullHttpRequest.getUri().equalsIgnoreCase("pull")
                    || fullHttpRequest.getUri().equalsIgnoreCase("/get")
                    || fullHttpRequest.getUri().equalsIgnoreCase("pull")) {

                Log.debug("Request receive to get a model");

                // TODO uuid parameter is not managed

                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK);

                UUIDModel uuidModel = modelService.getCurrentModel();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
                writer.write("uuid=" + uuidModel.getUUID().toString());
                writer.write("\n");
                writer.write("model=" + serializer.serialize(uuidModel.getModel()));
                writer.write("\n");
                writer.flush();

                httpResponse.content().writeBytes(stream.toByteArray());

                if (httpResponse.headers().get(CONTENT_LENGTH) == null) {
                    httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
                }
                httpResponse.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

                // Close the connection when the whole content is written out.
                channelHandlerContext.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
                channelHandlerContext.flush();
            } else if (fullHttpRequest.getUri().equalsIgnoreCase("/push")
                    || fullHttpRequest.getUri().equalsIgnoreCase("push")) {

                Log.debug("Request receive to apply a new model");

                String content = new String(Reader.readContent(fullHttpRequest.content()), "UTF-8");

                QueryStringDecoder decoder = new QueryStringDecoder(content, false);


                UUID uuid = UUID.fromString(decoder.parameters().get("uuid").get(0));


                ContainerRoot model = (ContainerRoot) loader.loadModelFromString(decoder.parameters().get("model").get(0)).get(0);


                boolean succeed = group.updateModel(model, uuid);

                if (succeed) {
                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HTTP_1_1, OK, Unpooled.copiedBuffer("DONE", CharsetUtil.UTF_8));
                    response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

                    // Close the connection as soon as the error message is sent.
                    channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    sendError(channelHandlerContext, REQUEST_TIMEOUT);
                }
            } else {
                // TODO manage /diff request
                Log.debug("Request receive but not managed: {}", fullHttpRequest.getUri());

                sendError(channelHandlerContext, NOT_FOUND);
            }
        }
    }
}
