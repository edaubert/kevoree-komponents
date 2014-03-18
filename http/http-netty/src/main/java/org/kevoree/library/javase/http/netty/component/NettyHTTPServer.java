package org.kevoree.library.javase.http.netty.component;

import io.netty.channel.ChannelHandler;
import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.http.api.commons.HTTPOperationTuple;
import org.kevoree.library.javase.http.api.server.AbstractHTTPServer;
import org.kevoree.library.javase.http.netty.NettyServer;

import java.util.HashMap;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/08/13
 * Time: 16:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class NettyHTTPServer extends AbstractHTTPServer {
    //    private boolean ssl;
    private NettyHTTPHandler handler;
    private NettyServer server;

    @Override
    public void start() throws Exception {
//        ssl = getDictionary().get("ssl") != null && "true".equalsIgnoreCase(getDictionary().get("ssl").toString());
        handler = new NettyHTTPHandler(this);
        server = new NettyServer(context.getInstanceName());
        server.start(port, handler, new HashMap<String, ChannelHandler>());
    }


    @Override
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    // TODO replace Object with a specific type and rename the parameter
    public void response(Object param) {
        if (param != null && param instanceof HTTPOperationTuple) {
            handler.response((HTTPOperationTuple) param);
        }
    }

}
