package org.apache.shiro.cas.grails

import grails.util.Holders
import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.shiro.SecurityUtils
import org.apache.shiro.UnavailableSecurityManagerException
import org.apache.shiro.web.util.WebUtils
import org.springframework.web.util.UriComponentsBuilder

class ShiroCasConfigUtils {
    @PackageScope
    static Log log = LogFactory.getLog(ShiroCasConfigUtils)

    static String serverUrl
    private static String serviceUrl
    private static String loginUrl
    private static String configLoginUrl
    private static String logoutUrl
    private static String configLogoutUrl
    private static String failureUrl
    private static String servicePath
    private static String failurePath
    private static def loginParameters
    static boolean hasDynamicServerName
    static String singleSignOutArtifactParameterName
    static String singleSignOutLogoutParameterName
    private static String filterChainDefinitions

    static void initialize(ConfigObject config) {
        def casConfig = config.security.shiro.cas

        singleSignOutArtifactParameterName = casConfig.singleSignOut.artifactParameterName ?: "ticket"
        singleSignOutLogoutParameterName = casConfig.singleSignOut.logoutParameterName ?: "logoutRequest"
        filterChainDefinitions = config.security.shiro.filter.filterChainDefinitions ?: ""
        hasDynamicServerName = casConfig.servicePath ? true : false
        serverUrl = stripTrailingSlash(casConfig.serverUrl ?: "")
        serviceUrl = stripTrailingSlash(casConfig.serviceUrl ?: "")
        failureUrl = casConfig.failureUrl ?: null

        configLoginUrl = casConfig.loginUrl ?: null
        configLogoutUrl = casConfig.logoutUrl ?: null
        loginParameters = casConfig.loginParameters ?: null

        if(hasDynamicServerName){
            servicePath = stripTrailingSlash(casConfig.servicePath ?: "")
            failurePath = casConfig.failurePath ?: null
            loginUrl = ""
            logoutUrl = ""

            if (!servicePath) {
                log.error("Invalid application configuration: security.shiro.cas.servicePath is required when enabling security.shiro.cas.dynamicServerName; it should be /mycontextpath/shiro-cas")
            }
        }
        else{
            loginUrl = serverUrl ? assembleLoginUrl() : ""
            logoutUrl = serverUrl ? assembleLogoutUrl() : ""
        }

        if (!serverUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        }
        if (!serviceUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serviceUrl is required; it should be http://host:port/mycontextpath/shiro-cas")
        }

    }

    static String getServiceUrl() {
        if(hasDynamicServerName && servicePath){
            try{
                def httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject())
                if(httpRequest) {
                    def builder = UriComponentsBuilder.fromHttpUrl(httpRequest.getRequestURL().toString()).replacePath(servicePath)
                    return builder.build().encode().toUriString()
                }
            }catch(UnavailableSecurityManagerException ex){
                log.info("Unable to get a dynamic serviceUrl, reverting to default.", ex)
            }
        }

        return serviceUrl
    }

    static String getFailureUrl() {
        if(hasDynamicServerName && failurePath){
            try{
                def httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject())
                if(httpRequest) {
                    def builder = UriComponentsBuilder.fromHttpUrl(httpRequest.getRequestURL().toString()).replacePath(failurePath)
                    return builder.build().encode().toUriString()
                }
            }catch(UnavailableSecurityManagerException ex){
                log.info("Unable to get a dynamic failureUrl, reverting to default.", ex)
            }
        }

        return failureUrl
    }

    static String getLoginUrl() {
        return hasDynamicServerName? assembleLoginUrl() : loginUrl
    }

    static String getLogoutUrl() {
        return hasDynamicServerName? assembleLogoutUrl() : logoutUrl
    }

    static boolean isSingleSignOutDisabled() {
        return Holders.config.security.shiro.cas.singleSignOut.disabled ?: false
    }

    private static String assembleLoginUrl() {
        def builder = configLoginUrl ?
                UriComponentsBuilder.fromHttpUrl(configLoginUrl) :
                UriComponentsBuilder.fromHttpUrl(serverUrl).path("/login").queryParam("service", getServiceUrl())

        if (loginParameters) {
            loginParameters.each { name, value ->
                builder.queryParam(name, value)
            }
        }

        return builder.build().encode().toUriString()
    }

    private static String assembleLogoutUrl() {
        def builder = configLogoutUrl ?
                UriComponentsBuilder.fromHttpUrl(configLogoutUrl) :
                UriComponentsBuilder.fromHttpUrl(serverUrl).path("/logout").queryParam("service", getServiceUrl())
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
