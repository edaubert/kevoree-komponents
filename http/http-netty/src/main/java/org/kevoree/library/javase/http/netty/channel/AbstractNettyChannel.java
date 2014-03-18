package org.kevoree.library.javase.http.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.*;
import org.kevoree.komponents.helpers.ModelManipulation;
import org.kevoree.library.javase.http.netty.NettyServer;
import org.kevoree.library.javase.http.netty.helpers.Reader;
import org.kevoree.log.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/14
 * Time: 08:35
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@ChannelType
public abstract class AbstractNettyChannel implements ChannelDispatch {
    @Param(optional = true, defaultValue = "8080", fragmentDependent = true)
    protected int port;
    @Param(optional = true, defaultValue = "5000")
    protected int timeout;

    @KevoreeInject
    protected ModelService modelService;
    @KevoreeInject
    protected BootstrapService bootstrapService;
    @KevoreeInject
    protected Context context;
    @KevoreeInject
    protected ChannelContext channelContext;

    //    private boolean ssl;
    private NettyDataHandler handler;
    private NettyServer server;

    private ObjectEncoderImpl encoder;

    public String getInstanceName() {
        return context.getInstanceName();
    }

    @Start
    public void start() throws Exception {
        encoder = new ObjectEncoderImpl();
        // TODO manage ssl
        handler = new NettyDataHandler(modelService, bootstrapService, context.getNodeName(), this);
        server = new NettyServer(context.getInstanceName());
        server.start(port, handler, new HashMap<String, ChannelHandler>());
    }

    @Stop
    public void stop() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
    }

    public InputStream sendData(Object payload, ContainerNode node) {

        byte[] bytes;
        if (payload instanceof String) {
            try {
                bytes = ((String) payload).getBytes("UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                ignored.printStackTrace();
                return null;
            }
        } else if (payload instanceof Serializable) {
            try {
                ByteBuf buf = Unpooled.buffer();
                encoder.encode(null, (Serializable) payload, buf);
                bytes = Reader.readContent(buf);
            } catch (Exception ignored) {
                ignored.printStackTrace();
                return null;
            }
        } else {
            Log.warn("Unable to manage payload from type {} on {}", payload.getClass(), context.getInstanceName());
            return null;
        }

        if (bytes != null) {
            // get ips
            List<String> ips = ModelManipulation.getIps((ContainerRoot) node.eContainer(), node.getName(), false);
            // get port
            int port = 8080;
            String value = ModelManipulation.getFragmentDictionaryValue(((ContainerRoot) node.eContainer()).findHubsByID(context.getInstanceName()), "port", node.getName());
            if (value != null) {
                try {
                    port = Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    ignored.printStackTrace();
                }
            }
            for (String ip : ips) {
                Log.info("Trying to send data on {}", "http://" + ip + ":" + port + "/");
                InputStream stream = sendRequest("http://" + ip + ":" + port + "/", new ByteArrayInputStream(bytes));
                if (stream != null) {
                    return stream;
                }
            }
            Log.warn("Unable to send data from {} to {} with {}", context.getNodeName(), node.getName(), context.getInstanceName());
            return null;
        } else {
            Log.warn("Something unexpected happens while trying to send data from {} to {} with {}", context.getNodeName(), node.getName(), context.getInstanceName());
            return null;
        }
    }

    private InputStream sendRequest(String urlString, InputStream content) {
        try {
            URL url = new URL(urlString);
            // TODO manage ssl
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[2048];
            int length = content.read(bytes);
            int contentLength = length;
            while (length != -1) {
                byteArrayStream.write(bytes, 0, length);
                length = content.read(bytes);
                contentLength += length;
            }

            connection.setRequestProperty("Content-Length", "" + contentLength);
            OutputStream wr = connection.getOutputStream();
            wr.write(byteArrayStream.toByteArray());
            wr.flush();

            InputStream rd = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            length = content.read(bytes);
            while (length != -1) {
                byteArrayOutputStream.write(bytes, 0, length);
                length = rd.read(bytes);
            }
            wr.close();
            rd.close();
            connection.disconnect();
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    class ObjectEncoderImpl extends ObjectEncoder {
        @Override
        protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
            super.encode(ctx, msg, out);
        }
    }
}
