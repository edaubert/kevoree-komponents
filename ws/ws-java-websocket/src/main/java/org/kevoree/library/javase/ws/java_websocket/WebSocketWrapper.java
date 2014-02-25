package org.kevoree.library.javase.ws.java_websocket;

import org.java_websocket.WebSocket;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/02/14
 * Time: 10:32
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class WebSocketWrapper {
    private WebSocket webSocket;
    private String uri;

    public WebSocketWrapper(WebSocket webSocket, String uri) {
        this.webSocket = webSocket;
        this.uri = uri;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public String getUri() {
        return uri;
    }
}
