package org.apache.shiro.cas.grails

import grails.util.Holders
import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.util.UriComponentsBuilder

class ShiroCasConfigUtils {
    @PackageScope
    static Log log = LogFactory.getLog(ShiroCasConfigUtils)

    static String serverUrl
    static String serviceUrl
    static String loginUrl
    static String logoutUrl
    static String failureUrl
    static String singleSignOutArtifactParameterName
    static String singleSignOutLogoutParameterName
    private static String filterChainDefinitions

    static void initialize(ConfigObject config) {
        def casConfig = config.security.shiro.cas
        serverUrl = stripTrailingSlash(casConfig.serverUrl ?: "")
        serviceUrl = stripTrailingSlash(casConfig.serviceUrl ?: "")
        loginUrl = serverUrl ? assembleLoginUrl(casConfig) : ""
        logoutUrl = serverUrl ? assembleLogoutUrl(casConfig) : ""
        failureUrl = casConfig.failureUrl ?: null
        singleSignOutArtifactParameterName = casConfig.singleSignOut.artifactParameterName ?: "ticket"
        singleSignOutLogoutParameterName = casConfig.singleSignOut.logoutParameterName ?: "logoutRequest"
        filterChainDefinitions = config.security.shiro.filter.filterChainDefinitions ?: ""
        if (!serverUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        }
        if (!serviceUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serviceUrl is required; it should be http://host:port/mycontextpath/shiro-cas")
        }
    }

    static boolean isSingleSignOutDisabled() {
        return Holders.config.security.shiro.cas.singleSignOut.disabled ?: false
    }

    private static String assembleLoginUrl(ConfigObject casConfig) {
        def params = casConfig.loginParameters
        def builder = casConfig.loginUrl ?
                UriComponentsBuilder.fromHttpUrl(casConfig.loginUrl) :
                UriComponentsBuilder.fromHttpUrl(serverUrl).path("/login").queryParam("service", serviceUrl)
        if (params) {
            params.each { name, value ->
                builder.queryParam(name, value)
            }
        }
        return builder.build().encode().toUriString()
    }

    private static String assembleLogoutUrl(ConfigObject casConfig) {
        def builder = casConfig.logoutUrl ?
                UriComponentsBuilder.fromHttpUrl(casConfig.logoutUrl) :
                UriComponentsBuilder.fromHttpUrl(serverUrl).path("/logout").queryParam("service", serviceUrl)
        return builder.build().encode().toUriString()
    }
    
    static String getShiroCasFilter() {
        def filters = new StringBuilder()
        if (!isSingleSignOutDisabled()) {
            // The SingleSignOutFilter must come before the CAS filter
            // https://wiki.jasig.org/display/CASC/Configuring+Single+Sign+Out
            filters.append("/shiro-cas=singleSignOutFilter,casFilter\n")
        } else {
            filters.append("/shiro-cas=casFilter\n")
        }
        filters.append(filterChainDefinitions)
        return filters.toString()
    }

    private static String stripTrailingSlash(String url) {
        return url?.endsWith("/") ? url[0..-2] : url
    }
}
