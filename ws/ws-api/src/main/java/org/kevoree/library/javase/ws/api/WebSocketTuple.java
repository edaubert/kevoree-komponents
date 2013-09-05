package org.kevoree.library.javase.ws.api;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 29/07/13
 * Time: 12:55
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class WebSocketTuple {
    public boolean broadcast;
    public long id;
    public String uri;
    public String message;

    public WebSocketTuple(long id, String uri, String message) {
        this.broadcast = false;
        this.id = id;
        this.uri = uri;
        this.message = message;
    }

    public WebSocketTuple(String uri, String message) {
        this.uri = uri;
        this.broadcast = true;
        this.message = message;
    }

    public WebSocketTuple(long id, String message) {
        this.broadcast = false;
        this.id = id;
        this.message = message;
    }

}
