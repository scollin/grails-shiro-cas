package org.apache.shiro.cas.grails

import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class ShiroCasConfigUtils {
    @PackageScope
    static Log log = LogFactory.getLog(ShiroCasConfigUtils)

    static String serverUrl
    static String serviceUrl
    static String loginUrl
    static String logoutUrl
    static String failureUrl
    private static String filterChainDefinitions

    static void initialize(ConfigObject config) {
        def casConfig = config.security.shiro.cas
        serverUrl = stripTrailingSlash(casConfig.serverUrl ?: "")
        serviceUrl = stripTrailingSlash(casConfig.serviceUrl ?: "")
        loginUrl = casConfig.loginUrl ?: "${serverUrl}/login?service=${serviceUrl}"
        logoutUrl = casConfig.logoutUrl ?: "${serverUrl}/logout?service=${serviceUrl}"
        failureUrl = casConfig.failureUrl ?: null
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

    private static String stripTrailingSlash(String url) {
        return url?.endsWith("/") ? url[0..-2] : url
    }
}
