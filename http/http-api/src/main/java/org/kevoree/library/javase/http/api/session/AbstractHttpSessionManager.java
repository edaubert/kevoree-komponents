package org.kevoree.library.javase.http.api.session;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Input;

import javax.servlet.http.HttpSession;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/01/14
 * Time: 10:15
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public abstract class AbstractHttpSessionManager {


    @Input
    public abstract HttpSession createHTTPSession();

    public abstract HttpSession getHTTPSession(String id);

    public abstract boolean removeHTTPSession(String id);

    @Input
    public HttpSession getHTTPSession(Object id) {
        return getHTTPSession(id.toString());
    }

    @Input
    public boolean removeHTTPSession(Object id) {
        return removeHTTPSession(id.toString());
    }

}
