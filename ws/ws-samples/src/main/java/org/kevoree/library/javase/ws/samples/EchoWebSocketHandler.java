package org.kevoree.library.javase.ws.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.ws.api.AbstractWebSocketHandler;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 29/07/13
 * Time: 13:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class EchoWebSocketHandler extends AbstractWebSocketHandler {
    @Override
    public void onOpen(long id, String uri, String message) {
        Log.info("Someone is now connected to the WebSocket server: id = '{}'", id);
    }

    @Override
    public void onMessage(long id, String uri, String message) {
        Log.info("receive a message from {} on {}: {}", id, getName(), message);
        send(id, message);
    }

    @Override
    public void onClose(long id, String uri, String message) {
        Log.info("Someone is now disconnected to the WebSocket server: id = '{}'", id);
    }
}
