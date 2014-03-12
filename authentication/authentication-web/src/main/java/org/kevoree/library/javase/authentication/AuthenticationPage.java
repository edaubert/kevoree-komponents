package org.kevoree.library.javase.authentication;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Output;
import org.kevoree.annotation.Param;
import org.kevoree.annotation.Start;
import org.kevoree.api.Callback;
import org.kevoree.api.Port;
import org.kevoree.library.javase.http.samples.pages.StaticFileHandler;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/12/13
 * Time: 14:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class AuthenticationPage extends StaticFileHandler {

    @Output(optional = false)
    protected Port authenticate;

    @Param(optional = true, defaultValue = "KEVOREE_AUTHENTICATION_ID")
    private String cookieName;

    @Param(optional = true, defaultValue = "30000")
    private Long sessionTime;

    @Param(optional = true, defaultValue = "index.html")
    private String redirectURI;

    private Random random;
    private Map<String, HttpSession> authenticatedSessions;

    @Start
    public void start() throws Exception {
        super.start();
        this.random = new Random();
        authenticatedSessions = new HashMap<String, HttpSession>();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getRequestedSessionId();
        if (authenticatedSessions.containsKey(sessionId)) {
            HttpSession session = authenticatedSessions.get(sessionId);
            if (session.getCreationTime() + sessionTime > System.currentTimeMillis()) {
                super.doGet(req, resp);
            } else {
                resp.setStatus(NO_RETURN_RESPONSE);
            }
        } else {
            super.doGet(req, resp);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final CallbackAuthentication callback = new CallbackAuthentication(req.getParameter("login").toString());
        new Thread() {
            @Override
            public void run() {
                authenticate.call(new SecurityInformation(req.getParameter("login").toString(), req.getParameter("password").toString()), callback);
            }
        }.start();
        if (callback.get(4000)) {
            HttpSession session = req.getSession(true);
            resp.encodeRedirectURL(req.getRequestURL() + "/" + redirectURI);
            authenticatedSessions.put(session.getId(), session);
            Cookie cookie = new Cookie(cookieName, session.getId());
            resp.addCookie(cookie);
        }
//        }
    }

    private class CallbackAuthentication implements Callback<Boolean> {

        private Boolean result;
        private String login;

        private CallbackAuthentication(String login) {
            this.login = login;
        }

        @Override
        public synchronized void onSuccess(Boolean result) {
            this.result = result;
            notify();
        }

        @Override
        public synchronized void onError(Throwable exception) {
            Log.info("Unable to authenticate: {}", exception, login);
            result = false;
            notify();
        }

        public synchronized Boolean get(int timeout) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                Log.warn("Unable to wait answer of a service...");
                Log.info("Unable to authenticate: {}", login);
            }
            if (result == null) {
                return false;
            } else {
                return result;
            }
        }

    }
}
