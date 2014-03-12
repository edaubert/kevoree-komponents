package org.kevoree.library.java.authentication.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.authentication.AuthenticationServer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/03/14
 * Time: 13:04
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class DummyAuthentication  extends AuthenticationServer {
    @Override
    public boolean authenticate(String login, String credentials) {
        return login.equals(credentials);
    }
}
