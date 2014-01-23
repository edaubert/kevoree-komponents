package org.kevoree.library.javase.authentication;

import org.kevoree.annotation.*;
import org.kevoree.log.Log;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/12
 * Time: 20:51
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class LDAPAuthentication extends AuthenticationServer {

    @Param(optional = false)
    protected String host;
    @Param(optional = false)
    protected String context;
    @Param(optional = false)
    protected String base;
    @Param(optional = false)
    protected String filter;
    @Param(optional = false)
    protected String loginKey;
    @Param(optional = true, defaultValue = "false")
    protected boolean ssl;
    @Param(optional = true)
    protected String trustStorePath;

    @Start
    @Stop
    public void dummy() {
    }


    private String getUUID(String login) throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, context);
        env.put(Context.PROVIDER_URL, host);
        DirContext ctx = new InitialDirContext(env);
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> items = ctx
                .search(base, "(&" + filter + "(" + loginKey + "=" + login + "))", sc);
        String result = null;
        while (items.hasMore()) {
            SearchResult item = items.next();
            if (item.getAttributes().get("uid") != null) {
                result = item.getAttributes().get("uid").get().toString();
            }
        }
        return result;
    }

    public boolean authenticate(String login, String password) {
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, context);
            env.put(Context.PROVIDER_URL, host);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            if (ssl) {
                if (trustStorePath != null) {
                    System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                }
                env.put(Context.SECURITY_PROTOCOL, "ssl");
            }
            env.put(Context.SECURITY_PRINCIPAL, "uid=" + getUUID(login)+ "," + base);
            env.put(Context.SECURITY_CREDENTIALS, password);
            DirContext ctx = new InitialDirContext(env);

            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

            ctx.search(base, filter, sc);
            return true;
        } catch (NamingException e) {
            Log.error("Unable to authenticate", e);
            return false;
        }
    }
}
