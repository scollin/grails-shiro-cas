package org.apache.shiro.cas.grails

import groovy.transform.PackageScope
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.cas.CasToken

class ShiroCasPrincipalManager {
    private static Set<Object> casPrincipals = []

    static void rememberPrincipalForToken(AuthenticationToken authenticationToken) {
        if (authenticationToken instanceof CasToken) {
            casPrincipals << authenticationToken.principal
        }
    }

    @PackageScope
    static boolean isFromCas(Object principal) {
        return casPrincipals.contains(principal)
    }

    @PackageScope
    static void forgetPrincipal(Object principal) {
        casPrincipals.remove(principal)
    }
}
