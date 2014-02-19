package org.kevoree.library.javase.http.api.server;

import org.kevoree.annotation.*;
import org.kevoree.api.Port;
import org.kevoree.library.javase.http.api.commons.HTTPOperationTuple;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/04/13
 * Time: 10:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "web")
@ComponentType
public abstract class AbstractHTTPServer {
    @Param(optional = true, defaultValue = "8080")
    protected int port;
    @Param(optional = true, defaultValue = "5000")
    protected long timeout;
    @Output
    protected Port request;

    @Output
    protected Port session;

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

    public long getTimeout() {
        return timeout;
    }

    @Start
    abstract public void start() throws Exception;

    @Stop
    abstract public void stop() throws Exception;

    @Input(optional = false)
    abstract public void response(/*HTTPOperationTuple*/Object param);

    public void request(/*HTTPOperationTuple*/Object param) {
        if (param != null && param instanceof HTTPOperationTuple && request != null) {
            request.send(param);
        }
    }
}