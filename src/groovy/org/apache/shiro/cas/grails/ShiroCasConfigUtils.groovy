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
        loginUrl =  addUrlParameters(casConfig.loginUrl ?: "${serverUrl}/login?service=${serviceUrl}", casConfig.loginParameters)
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
    
    private static String addUrlParameters(String url, ConfigObject parameters){
        if(parameters){
            def query = parameters.collect{param, val->
                "${URLEncoder.encode(param as String, "UTF-8")}=${URLEncoder.encode(val as String, "UTF-8")}"
            }.join('&')
            return "${url}${new URI(url).getQuery() ? '&' : '?'}${query}"
        }
        return url
    }

    static String getShiroCasFilter() {
        return "/shiro-cas=casFilter\n${filterChainDefinitions}"
    }

    private static String stripTrailingSlash(String url) {
        return url?.endsWith("/") ? url[0..-2] : url
    }
}
