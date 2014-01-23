package org.kevoree.library.javase.authentication;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Output;
import org.kevoree.api.Port;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/12/13
 * Time: 16:17
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public abstract class AuthenticationClient {

    @Output(optional = false)
    protected Port authenticate;
}
