package org.kevoree.library.javase.authentication;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/03/12
 * Time: 18:29
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public class TriskellINRIALDAPTester extends LDAPAuthentication {

    public TriskellINRIALDAPTester() {
        ssl = true;
        trustStorePath = "/home/edaubert/workspace/kevoree-various/kevoree-komponents/authentication/authentication-api/inria-ldap-keystore";
        host = "ldap://ildap1-ren.irisa.fr";
        context = "com.sun.jndi.ldap.LdapCtxFactory";
        base = "ou=people,dc=inria,dc=fr";
        filter = "(&(objectclass=inriaperson)(inriaentrystatus=valid)(ou=UR-Rennes))";//(inriaGroupMemberOf=cn=DIVERSE-ren,ou=groups,dc=inria,dc=fr))";
        loginKey = "inriaLogin";
    }

    public static void main(String[] args) {
        LDAPAuthentication authenticationService = new TriskellINRIALDAPTester();
        System.out.println(authenticationService.authenticate(new SecurityInformation(args[0], args[1])));

    }
}
