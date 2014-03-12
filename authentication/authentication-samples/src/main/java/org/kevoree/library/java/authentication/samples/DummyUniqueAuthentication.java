package org.kevoree.library.java.authentication.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;
import org.kevoree.library.javase.authentication.AuthenticationServer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/03/14
 * Time: 13:05
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class DummyUniqueAuthentication extends AuthenticationServer {

    @Param(optional = false)
    private String login;
    @Param(optional = false)
    private String credentials;

    @Override
    public boolean authenticate(String login, String credentials) {
        return login.equals(this.login) && credentials.equals(this.credentials);
    }
}
