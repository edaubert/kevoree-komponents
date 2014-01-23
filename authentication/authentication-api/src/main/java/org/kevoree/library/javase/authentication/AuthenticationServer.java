package org.kevoree.library.javase.authentication;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Input;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/12
 * Time: 21:09
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public abstract class AuthenticationServer {

    @Input(optional = true)
    public boolean authenticate(Object payload) {
        if (payload instanceof SecurityInformation) {
            return authenticate(((SecurityInformation)payload).getLogin(), ((SecurityInformation)payload).getCredentials());
        } else {
            return false;
        }
    }

    abstract boolean authenticate(String login, String credentials);
}
