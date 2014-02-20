package org.kevoree.library.javase.http.api.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/02/14
 * Time: 10:25
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class KevoreeHttpSessionBindingEvent extends HttpSessionBindingEvent {

    public KevoreeHttpSessionBindingEvent(HttpSession session, String name) {
        super(session, name);
    }

    public KevoreeHttpSessionBindingEvent(HttpSession session, String name, Object value) {
        super(session, name, value);
    }
}
