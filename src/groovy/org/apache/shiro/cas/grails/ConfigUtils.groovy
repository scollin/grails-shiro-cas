package org.apache.shiro.cas.grails

import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.cas.CasToken

/**
 * The Config utilies
 */
class ConfigUtils {
    private static Log log = LogFactory.getLog(ConfigUtils)

    private static Set<Object> casPrincipals = []

    static String serverUrl
    static String serviceUrl
    static String loginUrl
    static String logoutUrl
    static String failureUrl
    private static String filterChainDefinitions

    static void initialize(ConfigObject config) {
        serverUrl = stripTrailingSlash(config.security.shiro.cas.serverUrl)
        serviceUrl = stripTrailingSlash(config.security.shiro.cas.serviceUrl)
        loginUrl = config.security.shiro.cas.loginUrl ?: "${serverUrl}/login?service=${serviceUrl}"
        logoutUrl = config.security.shiro.cas.logoutUrl ?: "${serverUrl}/logout?service=${serviceUrl}"
        failureUrl = config.security.shiro.cas.failureUrl
        filterChainDefinitions = config.security.shiro.filter.filterChainDefinitions ?: ""
        if (!serverUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        }
        if (!serviceUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serviceUrl is required; it should be http://host:port/mycontextpath/shiro-cas")
        }
    }
    
    static String getShiroCasFilter() {
        return "/shiro-cas=casFilter\n${filterChainDefinitions}"
    }
    
    static void putPrincipal(AuthenticationToken authenticationToken) {
        if (authenticationToken instanceof CasToken) {
            casPrincipals << authenticationToken.principal
        }
    }

    @PackageScope
    static boolean isFromCas(Object principal) {
        return casPrincipals.contains(principal)
    }

    @PackageScope
    static void removePrincipal(Object principal) {
        casPrincipals.remove(principal)
    }

    private static String stripTrailingSlash(String url) {
        return url?.endsWith("/") ? url[0..-2] : url
    }
}