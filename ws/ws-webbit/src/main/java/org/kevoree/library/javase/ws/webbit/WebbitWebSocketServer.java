package org.kevoree.library.javase.ws.webbit;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.ws.api.AbstractWebSocketServer;
import org.kevoree.log.Log;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.HttpToWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 29/07/13
 * Time: 12:41
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class WebbitWebSocketServer extends AbstractWebSocketServer {
    WebServer server;
    private WebbitWebSocketHandler handler;

    private Map<Long, WebSocketConnection> connectionsFromId;
    private Map<String, List<WebSocketConnection>> connectionsFromUri;

    @Override
    public void start() throws Exception {
        connectionsFromId = new HashMap<Long, WebSocketConnection>();
        connectionsFromUri = new HashMap<String, List<WebSocketConnection>>();

        server = WebServers.createWebServer(port);
//        server.staleConnectionTimeout(Integer.parseInt(getDictionary().get("timeout").toString()));
        handler = new WebbitWebSocketHandler(this);
        server.add(new HttpToWebSocketHandler(handler));
        server.start().get();
    }

    public void addConnection(WebSocketConnection connection) {
        connectionsFromId.put(connection.hashCode() + 0l, connection);
        List<WebSocketConnection> connections = connectionsFromUri.get(connection.httpRequest().uri());
        if (connections == null) {
            connections = new ArrayList<WebSocketConnection>();
        }
        connections.add(connection);
        connectionsFromUri.put(connection.httpRequest().uri(), connections);
    }


    public void removeConnection(WebSocketConnection connection) {
        connectionsFromId.remove(connection.hashCode() + 0l);
        List<WebSocketConnection> connections = connectionsFromUri.get(connection.httpRequest().uri());
        if (connections != null) {
            connections.remove(connection);
            connectionsFromUri.put(connection.httpRequest().uri(), connections);
        }
    }

    @Override
    public void stop() throws Exception {
        port = -1;
        Future future = server.stop();
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            Log.warn("Time out when waiting the stop of the server. Maybe it is blocked!");
        }
    }

    @Override
    public void update() throws Exception {
//        if (port != Integer.parseInt(getDictionary().get("port").toString())) {
            stop();
            start();
//        }
    }

    @Override
    public void send(long id, String message) {
        if (connectionsFromId.containsKey(id)) {
            connectionsFromId.get(id).send(message);
        }
    }

    @Override
    public void broadcast(String uri, String message) {
        if (connectionsFromUri.get(uri) != null) {
            for (WebSocketConnection connection : connectionsFromUri.get(uri)) {
                connection.send(message);
            }
        }
    }
}
