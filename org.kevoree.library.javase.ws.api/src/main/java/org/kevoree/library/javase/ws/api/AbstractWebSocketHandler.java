package org.kevoree.library.javase.ws.api;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
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
@Provides({
        @ProvidedPort(name = "onOpen", type = PortType.MESSAGE),
        @ProvidedPort(name = "onMessage", type = PortType.MESSAGE),
        @ProvidedPort(name = "onClose", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "send", type = PortType.MESSAGE),
        @RequiredPort(name = "broadcast", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "urlPattern", optional = true, defaultValue = "/")
})
public abstract class AbstractWebSocketHandler extends AbstractComponentType {

    protected String urlPatternRegex;

    public abstract void onOpen(long id, String uri, String message);
    public abstract void onMessage(long id, String uri, String message);
    public abstract void onClose(long id, String uri, String message);

    @Start
    public void start() {
        urlPatternRegex = getDictionary().get("urlPattern").toString().replaceAll("\\*{2,}", ".*")/*.replaceAll("[^.]\\*+", "/?[^/]*")*/;
    }

    @Update
    public void update() {
        if (!urlPatternRegex.equals(getDictionary().get("urlPattern").toString().replaceAll("\\*{2,}", ".*")/*.replaceAll("[^.]\\*+", "/?[^/]*")*/)) {
//            stop();
            start();
        }
    }

    protected String getUrlPatternWithoutRegex() {
        return urlPatternRegex.replaceAll("\\.\\*", "");
    }

    protected String getLastParam(String uri) {
        String result = uri;
        String urlPattern = this.urlPatternRegex;
        Pattern p = Pattern.compile("\\{(\\w+)\\}");
        Matcher m = p.matcher(urlPattern);
        while (m.find()) {
            urlPattern = urlPattern.replace("{" + m.group(1) + "}", ".*");
        }

        String regex = urlPattern.replace(".", "\\.").replaceAll("\\*{2,}", "(.*)")/*.replaceAll("[^.]\\*+", "(/?[^/]*)")*/;
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if (m.find()) {
            result = result.replace(m.group(1), "");
        }
        return result;
    }

    private boolean check(String url) {
        Log.debug("Checking url in component '{}' with urlPattern '{}' and url '{}'", getName(), urlPatternRegex, url);
        Pattern pattern = Pattern.compile(urlPatternRegex);
        Matcher m = pattern.matcher(url);
        return m.matches();
    }


    // TODO change type and name of the parameter
    @Port(name = "onOpen")
    public void onOpen(Object msg){
        if (msg instanceof WebSocketTuple && ((WebSocketTuple)msg).uri != null && check(((WebSocketTuple)msg).uri)) {
            onOpen(((WebSocketTuple)msg).id, ((WebSocketTuple)msg).uri, ((WebSocketTuple)msg).message);
        }
    }
    // TODO change type and name of the parameter
    @Port(name = "onMessage")
    public void onMessage(Object msg){
        if (msg instanceof WebSocketTuple && ((WebSocketTuple)msg).uri != null && check(((WebSocketTuple)msg).uri)) {
            onMessage(((WebSocketTuple) msg).id, ((WebSocketTuple)msg).uri, ((WebSocketTuple) msg).message);
        }
    }
    // TODO change type and name of the parameter
    @Port(name = "onClose")
    public void onClose(Object msg){
        if (msg instanceof WebSocketTuple && ((WebSocketTuple)msg).uri != null && check(((WebSocketTuple)msg).uri)) {
            onClose(((WebSocketTuple) msg).id, ((WebSocketTuple)msg).uri, ((WebSocketTuple) msg).message);
        }
    }

    public void send(long id, String message) {
        if (isPortBinded("send")) {
            getPortByName("send", MessagePort.class).process(new WebSocketTuple(id, message));
        }
    }

    public void broadcast(String uri, String message) {
        if (isPortBinded("broadcast") && uri != null && check(uri)) {
            getPortByName("broadcast", MessagePort.class).process(new WebSocketTuple(uri, message));
        }
    }
}
