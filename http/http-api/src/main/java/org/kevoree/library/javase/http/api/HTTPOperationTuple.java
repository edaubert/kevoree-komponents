package org.kevoree.library.javase.http.api;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/04/13
 * Time: 14:00
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class HTTPOperationTuple {
    public KevoreeHTTPServletRequest request;
    public KevoreeHTTPServletResponse response;

    public HTTPOperationTuple(KevoreeHTTPServletRequest request, KevoreeHTTPServletResponse response) {
        this.request = request;
        this.response = response;
    }
}
