package org.kevoree.library.javase.http.api.commons;

import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletRequest;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletResponse;
import org.kevoree.library.javase.http.api.server.AbstractHTTPServer;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/09/13
 * Time: 10:15
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class Monitor {
    private long timeout;
    private KevoreeHTTPServletRequest request;
    private KevoreeHTTPServletResponse response;
    private AbstractHTTPServer server;


    public Monitor(long timeout, AbstractHTTPServer server) {
        this.timeout = timeout;
        this.server = server;
    }

    public synchronized HTTPOperationTuple request(final HTTPOperationTuple param) throws InterruptedException {
        response = null;
        request = param.request;
        new Thread() {
            @Override
            public void run() {
                // This thread avoid the synchronous call from the port (if the channel is a synchronous one)
                server.request(param);
            }
        }.start();
        wait(timeout);
        if (response == null) {
            param.response.setStatus(408);
        } else {
            param.response = response;
        }
        return param;
    }

    public synchronized void response(HTTPOperationTuple param) {
        if (param.request == request) {
            response = param.response;
            notify();
        } else {
            Log.info("Timeout exceeds for request uri: {}", param.request.getRequestURI());
        }
    }
}
