package org.kevoree.library.javase.ws.api;

import org.kevoree.annotation.*;
import org.kevoree.api.Context;
import org.kevoree.api.Port;
import org.kevoree.log.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public abstract class AbstractWebSocketHandler {

    @Param(optional = true, defaultValue = "/")
    protected String urlPattern;

    @Output(optional = true)
    protected Port send;
    @Output(optional = true)
    protected Port broadcast;

    @KevoreeInject
    protected Context cmpContext;

    public abstract void onOpen(long id, String uri, String message);
    public abstract void onMessage(long id, String uri, String message);
    public abstract void onClose(long id, String uri, String message);

    private boolean check(String url) {
        Log.debug("Checking url in component '{}' with urlPattern '{}' and url '{}'", cmpContext.getInstanceName(), urlPattern, url);
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher m = pattern.matcher(url);
        return m.matches();
    }


    // TODO change type and name of the parameter
    @Input(optional = true)
    public void onOpen(Object msg){
        if (msg instanceof WebSocketTuple && ((WebSocketTuple)msg).uri != null && check(((WebSocketTuple)msg).uri)) {
            Log.debug("The request '{} => {}' is accepted by '{}.onOpen' with urlPattern '{}' ", ((WebSocketTuple) msg).id, ((WebSocketTuple) msg).uri, cmpContext.getInstanceName(), urlPattern);
            onOpen(((WebSocketTuple)msg).id, ((WebSocketTuple)msg).uri, ((WebSocketTuple)msg).message);
        }
    }
    // TODO change type and name of the parameter
    @Input(optional = true)
    public void onMessage(Object msg){
        if (msg instanceof WebSocketTuple && ((WebSocketTuple)msg).uri != null && check(((WebSocketTuple)msg).uri)) {
            Log.debug("The request '{} => {}/{}' is accepted by '{}.onMessage' with urlPattern '{}' ", ((WebSocketTuple) msg).id, ((WebSocketTuple) msg).uri, ((WebSocketTuple) msg).message, cmpContext.getInstanceName(), urlPattern);
            onMessage(((WebSocketTuple) msg).id, ((WebSocketTuple) msg).uri, ((WebSocketTuple) msg).message);
        }
    }
    // TODO change type and name of the parameter
    @Input(optional = true)
    public void onClose(Object msg){
        if (msg instanceof WebSocketTuple && ((WebSocketTuple)msg).uri != null && check(((WebSocketTuple)msg).uri)) {
            Log.debug("The request '{} => {}' is accepted by '{}.onClose' with urlPattern '{}' ", ((WebSocketTuple) msg).id, ((WebSocketTuple) msg).uri, cmpContext.getInstanceName(), urlPattern);
            onClose(((WebSocketTuple) msg).id, ((WebSocketTuple)msg).uri, ((WebSocketTuple) msg).message);
        }
    }

    public void send(long id, String message) {
        // FIXME how to test if the port is connected ?
//        if (isPortBinded("send")) {
            send.send(new WebSocketTuple(id, message));
//        }
    }

    public void broadcast(String uri, String message) {
        // FIXME how to test if the port is connected ?
//        if (isPortBinded("broadcast") && uri != null && check(uri)) {
            send.send(new WebSocketTuple(uri, message));
//        }
    }
}
