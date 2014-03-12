package org.kevoree.library.javase.ws.java_websocket;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.ws.api.AbstractWebSocketServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/02/14
 * Time: 10:08
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class JavaWebsocketServer extends AbstractWebSocketServer {

    private WebSocketServerImpl handler;
    private Map<Long, WebSocketWrapper> connectionsFromId;
    private Map<String, List<WebSocketWrapper>> connectionsFromUri;

    @Override
    public void start() throws Exception {
        connectionsFromId = new HashMap<Long, WebSocketWrapper>();
        connectionsFromUri = new HashMap<String, List<WebSocketWrapper>>();

        handler = new WebSocketServerImpl(port, this);
        handler.start();
    }

    @Override
    public void stop() throws Exception {
        if (handler != null) {
            handler.stop();
        }
    }

    @Override
    public void send(long id, String message) {
        if (connectionsFromId.containsKey(id)) {
            connectionsFromId.get(id).getWebSocket().send(message);
        }
    }

    @Override
    public void broadcast(String uri, String message) {
        if (connectionsFromUri.get(uri) != null) {
            for (WebSocketWrapper connection : connectionsFromUri.get(uri)) {
                connection.getWebSocket().send(message);
            }
        }
    }

    public void addConnection(WebSocketWrapper connection) {
        connectionsFromId.put(connection.hashCode() + 0l, connection);
        List<WebSocketWrapper> connections = connectionsFromUri.get(connection.getUri());
        if (connections == null) {
            connections = new ArrayList<WebSocketWrapper>();
        }
        connections.add(connection);
        connectionsFromUri.put(connection.getUri(), connections);
    }


    public void removeConnection(WebSocketWrapper connection) {
        connectionsFromId.remove(connection.hashCode() + 0l);
        List<WebSocketWrapper> connections = connectionsFromUri.get(connection.getUri());
        if (connections != null) {
            connections.remove(connection);
            connectionsFromUri.put(connection.getUri(), connections);
        }
    }
}
