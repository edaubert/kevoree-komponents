package org.kevoree.library.javase.ws.webbit;

import org.kevoree.log.Log;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 29/07/13
 * Time: 13:06
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class WebbitWebSocketHandler implements WebSocketHandler {
    private static final String REPO_URL = "https://github.com/edaubert/kevoree-komponents";
    private WebbitWebSocketServer server;

    public WebbitWebSocketHandler(WebbitWebSocketServer server) {
        this.server = server;
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Throwable {
        server.addConnection(connection);
        Log.debug("New request to handle on open: {}", connection.httpRequest().uri());
        server.onOpen(connection.hashCode(), connection.httpRequest().uri());
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Throwable {
        server.removeConnection(connection);
        Log.debug("New request to handle on close: {}", connection.httpRequest().uri());
        server.onClose(connection.hashCode(), connection.httpRequest().uri());
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
        Log.debug("New request to handle on message: {}", connection.httpRequest().uri());
//        Log.debug("Message received on internal handler of the websocket server: {} on {}", msg, server.getName());
        server.onMessage(connection.hashCode(), connection.httpRequest().uri(), msg);
    }

    @Override
    public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
        throw new Exception("Method 'onMessage(WebSocketConnection, byte[])' is not currently managed. Please submit an issue to " + REPO_URL);
    }

    @Override
    public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
        throw new Exception("Method 'onMessage(WebSocketConnection, byte[])' is not currently managed. Please submit an issue to " + REPO_URL);
    }

    @Override
    public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
        throw new Exception("Method 'onMessage(WebSocketConnection, byte[])' is not currently managed. Please submit an issue to " + REPO_URL);
    }
}
