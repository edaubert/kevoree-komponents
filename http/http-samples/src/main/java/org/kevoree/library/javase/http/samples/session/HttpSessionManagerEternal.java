package org.kevoree.library.javase.http.samples.session;

import org.kevoree.library.javase.http.api.session.HttpSessionManager;

import javax.servlet.http.HttpSession;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/01/14
 * Time: 13:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class HttpSessionManagerEternal extends HttpSessionManager {
    @Override
    public HttpSession createHTTPSession() {
        return null;
    }

    @Override
    public HttpSession getHTTPSession(String id) {
        return null;
    }

    @Override
    public boolean removeHTTPSession(String id) {
        return false;
    }
}
