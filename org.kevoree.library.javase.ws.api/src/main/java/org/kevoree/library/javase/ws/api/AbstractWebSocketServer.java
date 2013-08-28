package org.kevoree.library.javase.ws.api;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

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
@DictionaryType({
        @DictionaryAttribute(name = "port" , defaultValue = "8080"),
        @DictionaryAttribute(name = "timeout" , defaultValue = "5000", optional = true)
})
@Requires({
        @RequiredPort(name = "onOpen", type = PortType.MESSAGE),
        @RequiredPort(name = "onMessage", type = PortType.MESSAGE),
        @RequiredPort(name = "onClose", type = PortType.MESSAGE)
})
@Provides({
        @ProvidedPort(name = "send", type = PortType.MESSAGE),
        @ProvidedPort(name = "broadcast", type = PortType.MESSAGE)
})
public abstract class AbstractWebSocketServer extends AbstractComponentType {
    @Start
    abstract public void start() throws Exception;

    @Stop
    abstract public void stop() throws Exception;

    @Update
    abstract public void update() throws Exception;

    abstract public void send(long id, String message);
    abstract public void broadcast(String uri, String message);

    // TODO change type and name of the parameter
    @Port(name = "send")
    public void send(Object msg) {
        if (msg instanceof WebSocketTuple) {
            send(((WebSocketTuple)msg).id, ((WebSocketTuple)msg).message);
        }
    }

    // TODO change type and name of the parameter
    @Port(name = "broadcast")
    public void broadcast(Object msg) {
        if (msg instanceof WebSocketTuple) {
            broadcast(((WebSocketTuple) msg).uri, ((WebSocketTuple) msg).message);
        }
    }

    public void onOpen(long id, String uri) {
        if (isPortBinded("onOpen")) {
            getPortByName("onOpen", MessagePort.class).process(new WebSocketTuple(id, uri));
        }
    }

    public void onMessage(long id, String uri, String message) {
        if (isPortBinded("onMessage")) {
            getPortByName("onMessage", MessagePort.class).process(new WebSocketTuple(id, uri, message));
        }
    }

    public void onClose(long id, String uri) {
        if (isPortBinded("onClose")) {
            getPortByName("onClose", MessagePort.class).process(new WebSocketTuple(id, uri));
        }
    }
}
