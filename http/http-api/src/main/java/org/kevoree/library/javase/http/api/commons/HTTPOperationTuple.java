package org.kevoree.library.javase.http.api.commons;

import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletRequest;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletResponse;

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
    public Monitor monitor;

    public HTTPOperationTuple(KevoreeHTTPServletRequest request, KevoreeHTTPServletResponse response, Monitor monitor) {
        this.request = request;
        this.response = response;
        this.monitor = monitor;
    }
}
