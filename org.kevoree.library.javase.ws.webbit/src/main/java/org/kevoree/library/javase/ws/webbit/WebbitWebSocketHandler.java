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
    private WebbitWebSocketServer server;

    public WebbitWebSocketHandler(WebbitWebSocketServer server) {
        this.server = server;
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Throwable {
        server.addConnection(connection);
        Log.debug(connection.httpRequest().uri());
        server.onOpen(connection.hashCode(), connection.httpRequest().uri());
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Throwable {
        server.removeConnection(connection);
        server.onClose(connection.hashCode(), connection.httpRequest().uri());
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
        Log.debug("Message received on internal handler of the websocket server: {} on {}", msg, server.getName());
        server.onMessage(connection.hashCode(), connection.httpRequest().uri(), msg);
    }

    @Override
    public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
        throw new Exception("Method 'onMessage(WebSocketConnection, byte[])' is not currently managed. Please submit an issue to https://bitbucket.org/edaubert/kevoree-komponents/issues");
    }

    @Override
    public void onPing(WebSocketConnection connection, byte[] msg) throws Throwable {
        throw new Exception("Method 'onMessage(WebSocketConnection, byte[])' is not currently managed. Please submit an issue to https://bitbucket.org/edaubert/kevoree-komponents/issues");
    }

    @Override
    public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
        throw new Exception("Method 'onMessage(WebSocketConnection, byte[])' is not currently managed. Please submit an issue to https://bitbucket.org/edaubert/kevoree-komponents/issues");
    }
}
