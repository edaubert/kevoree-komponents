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
    public long id;
    public String message;

    public WebSocketTuple(long id, String message) {
        this.id = id;
        this.message = message;
    }
}
