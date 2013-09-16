package org.kevoree.library.javase.http.api;

import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/09/13
 * Time: 10:15
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public
class Monitor {
    private long timeout;
    private KevoreeHTTPServletRequest request;
    private KevoreeHTTPServletResponse response;
    private AbstractHTTPServer server;


    public Monitor(long timeout, AbstractHTTPServer server) {
        this.timeout = timeout;
        this.server = server;
    }

    public synchronized HTTPOperationTuple request(HTTPOperationTuple param) throws InterruptedException {
        response = null;
        request = param.request;
        server.request(param);
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
            Log.warn("timeout exceeds for request uri: {}", param.request.getRequestURI());
        }
    }

        /*synchronized HTTPOperationTuple error(HTTPOperationTuple param) throws InterruptedException {
            response = null;
            server.error(param);
            wait(timeout);
            if (response == null) {
                param.response.setStatus(404);
            } else {
                param.response = response;
            }
            return param;

        }*/

}
