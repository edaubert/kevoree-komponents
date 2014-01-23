package org.kevoree.library.javase.authentication;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/01/14
 * Time: 09:23
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SecurityInformation {
    private String login;
    private String credentials;

    public SecurityInformation(String login, String credentials) {
        this.login = login;
        this.credentials = credentials;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
}
