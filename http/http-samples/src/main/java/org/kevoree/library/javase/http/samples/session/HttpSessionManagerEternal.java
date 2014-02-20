package org.kevoree.library.javase.http.samples.session;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.library.javase.http.api.session.AbstractHttpSessionManager;
import org.kevoree.library.javase.http.api.session.KevoreeHttpSession;
import org.kevoree.library.javase.http.api.session.SimpleKevoreeHttpSessionImpl;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/01/14
 * Time: 13:11
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class HttpSessionManagerEternal extends AbstractHttpSessionManager {

    protected Map<String, KevoreeHttpSession> sessions;


    @Start
    public void start() {
        sessions = Collections.synchronizedMap(new HashMap<String, KevoreeHttpSession>());
    }

    @Stop
    public void stop() {
        invalidateAll();
        sessions.clear();
    }

    private void invalidateAll() {
        for (String key : sessions.keySet()) {
            sessions.get(key).invalidate();
        }
    }

    @Override
    public HttpSession createHTTPSession() {
        String id = UUID.randomUUID().toString();
        KevoreeHttpSession session = new SimpleKevoreeHttpSessionImpl(id, -1);
        sessions.put(id, session);
        return session;
    }

    @Override
    public HttpSession getHTTPSession(String id) {
        // FIXME how to ensure consistency if multiple services (potentially host in different nodes) request the same session ?
        KevoreeHttpSession session = sessions.get(id);
        if (session.isInvalidated()) {
            removeHTTPSession(id);
        }
        return session;
    }

    @Override
    public boolean removeHTTPSession(String id) {
        HttpSession session = sessions.remove(id);
        if (session != null) {
            session.invalidate();
            return true;
        }
        return false;
    }
}
