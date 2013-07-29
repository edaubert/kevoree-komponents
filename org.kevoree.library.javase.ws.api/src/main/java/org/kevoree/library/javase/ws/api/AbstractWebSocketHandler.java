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
@ComponentFragment
@Provides({
        @ProvidedPort(name = "onOpen", type = PortType.MESSAGE),
        @ProvidedPort(name = "onMessage", type = PortType.MESSAGE),
        @ProvidedPort(name = "onClose", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "send", type = PortType.MESSAGE),
})
@DictionaryType({
        @DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/")
})
public abstract class AbstractWebSocketHandler extends AbstractComponentType {

    public abstract void onOpen(long id, String message);
    public abstract void onMessage(long id, String message);
    public abstract void onClose(long id, String message);

    // TODO change type and name of the parameter
    @Port(name = "onOpen")
    public void onOpen(Object msg){
        if (msg instanceof WebSocketTuple) {
            onOpen(((WebSocketTuple)msg).id, ((WebSocketTuple)msg).message);
        }
    }
    // TODO change type and name of the parameter
    @Port(name = "onMessage")
    public void onMessage(Object msg){
        if (msg instanceof WebSocketTuple) {
            onMessage(((WebSocketTuple) msg).id, ((WebSocketTuple) msg).message);
        }
    }
    // TODO change type and name of the parameter
    @Port(name = "onClose")
    public void onClose(Object msg){
        if (msg instanceof WebSocketTuple) {
            onClose(((WebSocketTuple) msg).id, ((WebSocketTuple) msg).message);
        }
    }

    public void send(long id, String message) {
        if (isPortBinded("send")) {
            getPortByName("send", MessagePort.class).process(new WebSocketTuple(id, message));
        }
    }
}
