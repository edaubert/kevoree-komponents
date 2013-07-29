package org.kevoree.library.javase.ws.webbit;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.ws.api.AbstractWebSocketServer;
import org.kevoree.log.Log;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;

import java.util.HashMap;
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
    private int port;
    WebServer server;
    private WebbitWebSocketHandler handler;

    private Map<Long, WebSocketConnection> connections;

    @Override
    public void start() throws Exception {
        port = Integer.parseInt(getDictionary().get("port").toString());
        connections = new HashMap<Long, WebSocketConnection>();

        server = WebServers.createWebServer(port);
//        server.staleConnectionTimeout(Integer.parseInt(getDictionary().get("timeout").toString()));
        handler = new WebbitWebSocketHandler(this);
        server.add("/", handler);
        server.start().get();
    }

    public void addConnection(WebSocketConnection connection) {
        connections.put(connection.hashCode() + 0l, connection);
    }


    public void removeConnection(WebSocketConnection connection) {
        connections.remove(connection.hashCode() + 0l);
    }

    @Override
    public void stop() throws Exception {
        port = -1;
        Future future = server.stop();
        try {
            future.get(Integer.parseInt(getDictionary().get("timeout").toString()), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            Log.warn("Time out when waiting the stop of the server. Maybe it is blocked!");
        }
    }

    @Override
    public void update() throws Exception {
        if (port != Integer.parseInt(getDictionary().get("port").toString())) {
            stop();
            start();
        }
    }

    @Override
    public void send(long id, String message) {
        if (connections.containsKey(id)) {
            connections.get(id).send(message);
        }
    }

    @Override
    public void broadcast(String message) {
        for (long id : connections.keySet()) {
            connections.get(id).send(message);
        }
    }
}
