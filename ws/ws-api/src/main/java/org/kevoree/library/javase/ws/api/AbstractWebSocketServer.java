package org.kevoree.library.javase.ws.api;

import org.kevoree.annotation.*;
import org.kevoree.api.Port;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 29/07/13
 * Time: 10:07
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "web")
@ComponentType
public abstract class AbstractWebSocketServer {

    @Param(optional = true, defaultValue = "8080")
    protected int port;
    @Param(optional = true, defaultValue = "5000")
    protected long timeout;

    @Output(optional = true)
    protected Port onOpen;
    @Output(optional = true)
    protected Port onMessage;
    @Output(optional = true)
    protected Port onClose;

    public void setPort(int port) throws Exception {
        if (this.port != port) {
            this.port = port;
            stop();
            start();
        }
    }

    public void setTimeout(long timeout) throws Exception {
        if (this.timeout != timeout) {
            this.timeout = timeout;
            stop();
            start();
        }
    }

    @Start
    abstract public void start() throws Exception;

    @Stop
    abstract public void stop() throws Exception;

    abstract public void send(long id, String message);

    abstract public void broadcast(String uri, String message);

    // TODO change type and name of the parameter
    @Input(optional = true)
    public void send(Object msg) {
        if (msg instanceof WebSocketTuple) {
            send(((WebSocketTuple) msg).id, ((WebSocketTuple) msg).message);
        }
    }

    // TODO change type and name of the parameter
    @Input(optional = true)
    public void broadcast(Object msg) {
        if (msg instanceof WebSocketTuple) {
            broadcast(((WebSocketTuple) msg).uri, ((WebSocketTuple) msg).message);
        }
    }

    public void onOpen(long id, String uri) {
        // FIXME how to test if the port is connected ?
//        if (isPortBinded("onOpen")) {
        onOpen.send(new WebSocketTuple(uri, id));
//        }
    }

    public void onMessage(long id, String uri, String message) {
        // FIXME how to test if the port is connected ?
//        if (isPortBinded("onMessage")) {
        onMessage.send(new WebSocketTuple(id, uri, message));
//        }
    }

    public void onClose(long id, String uri) {
        // FIXME how to test if the port is connected ?
//        if (isPortBinded("onClose")) {
        onClose.send(new WebSocketTuple(id, uri));
//        }
    }
}
