package org.kevoree.library.javase.ws.java_websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.kevoree.log.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/02/14
 * Time: 10:10
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class WebSocketServerImpl extends WebSocketServer {
    private JavaWebsocketServer server;

    private Map<WebSocket, WebSocketWrapper> webSocketWrappers;

    public WebSocketServerImpl(int port, JavaWebsocketServer server) {
        super(new InetSocketAddress(port));
        this.server = server;
        webSocketWrappers = new HashMap<WebSocket, WebSocketWrapper>();
    }

    @Override
    public void stop(int timeout) throws IOException, InterruptedException {
        webSocketWrappers.clear();
        webSocketWrappers = null;
        super.stop(timeout);
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake clientHandshake) {
        WebSocketWrapper wrapper = new WebSocketWrapper(connection, clientHandshake.getResourceDescriptor());
        webSocketWrappers.put(connection, wrapper);
        server.addConnection(wrapper);
        Log.debug("New request to handle on open: {}", wrapper.getUri());
        server.onOpen(wrapper.hashCode(), clientHandshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        WebSocketWrapper wrapper = webSocketWrappers.get(connection);
        server.removeConnection(wrapper);
        Log.debug("New request to handle on close: {}", wrapper.getUri());
        server.onClose(wrapper.hashCode(), wrapper.getUri());
    }

    @Override
    public void onMessage(WebSocket connection, String msg) {
        WebSocketWrapper wrapper = webSocketWrappers.get(connection);
        Log.debug("New request to handle on message: {}", wrapper.getUri());
        server.onMessage(wrapper.hashCode(),wrapper.getUri(), msg);
    }

    @Override
    public void onError(WebSocket connection, Exception e) {
//        WebSocketWrapper wrapper = webSocketWrappers.get(connection);
//        Log.debug("New request to handle on message: {}", wrapper.getUri());
    }
}
