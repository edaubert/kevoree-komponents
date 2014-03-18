package org.kevoree.library.javase.http.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.*;
import org.kevoree.komponents.helpers.ModelManipulation;
import org.kevoree.library.javase.http.netty.NettyClient;
import org.kevoree.library.javase.http.netty.NettyClientHandler;
import org.kevoree.library.javase.http.netty.NettyClientOutput;
import org.kevoree.library.javase.http.netty.NettyServer;
import org.kevoree.library.javase.http.netty.helpers.Reader;
import org.kevoree.log.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    private NettyDataServerHandler handler;
    private NettyServer server;
    private NettyClientHandler clientHandler;
    private NettyClient client;

    private DataCodec codec;

    public String getInstanceName() {
        return context.getInstanceName();
    }

    @Start
    public void start() throws Exception {
        // TODO manage ssl
        codec = new DataCodec(modelService, bootstrapService, context.getInstanceName(), this);

        handler = new NettyDataServerHandler(modelService, bootstrapService, context.getNodeName(), this);
        server = new NettyServer(context.getInstanceName());
        server.start(port, handler, new HashMap<String, ChannelHandler>());

        clientHandler = new NettyDataClientHandler();
        client = new NettyClient();
        client.start(clientHandler, new HashMap<String, ChannelHandler>());
    }

    @Stop
    public void stop() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
    }

    public abstract Object dispatchLocal(Object payload);

    public InputStream sendData(Object payload, ContainerNode node) {

        ByteBuf buf = Unpooled.buffer();
        byte[] bytes;
        try {
            codec.encode(payload, buf);
            bytes = Reader.readContent(buf);
        } catch (Exception ignored) {
            ignored.printStackTrace();
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
                NettyClientOutput output = client.sendRequest(ip, port, "/", new ByteArrayInputStream(bytes));
                if (output != null && output.getContent() != null) {
                    return output.getContent();
                }
            }
            Log.warn("Unable to send data from {} to {} with {}", context.getNodeName(), node.getName(), context.getInstanceName());
            return null;
        } else {
            Log.warn("Unable to send data from {} to {} with {}", context.getNodeName(), node.getName(), context.getInstanceName());
            return null;
        }
    }
}
