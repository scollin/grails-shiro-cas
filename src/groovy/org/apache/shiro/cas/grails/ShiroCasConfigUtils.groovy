package org.apache.shiro.cas.grails

import grails.util.Holders
import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.shiro.SecurityUtils
import org.apache.shiro.UnavailableSecurityManagerException
import org.apache.shiro.web.util.WebUtils
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpServletRequest

class ShiroCasConfigUtils {
    @PackageScope
    static Log log = LogFactory.getLog(ShiroCasConfigUtils)

    private static String serverUrl
    private static String defaultServiceUrl
    private static String defaultFailureUrl
    private static String configuredLoginUrl
    private static String configuredLogoutUrl
    private static String servicePath
    private static String failurePath
    private static Map loginParameters
    private static String singleSignOutArtifactParameterName
    private static String singleSignOutLogoutParameterName
    private static String filterChainDefinitions

    static void initialize(ConfigObject config) {
        def casConfig = config.security.shiro.cas

        singleSignOutArtifactParameterName = casConfig.singleSignOut.artifactParameterName ?: "ticket"
        singleSignOutLogoutParameterName = casConfig.singleSignOut.logoutParameterName ?: "logoutRequest"
        filterChainDefinitions = config.security.shiro.filter.filterChainDefinitions ?: ""

        serverUrl = stripTrailingSlash(casConfig.serverUrl ?: "")
        defaultServiceUrl = stripTrailingSlash(casConfig.serviceUrl ?: "")

        if (!serverUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        }

        if (!defaultServiceUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serviceUrl is required; it should be http://host:port/mycontextpath/shiro-cas")
        }

        defaultFailureUrl = casConfig.failureUrl ?: null
        configuredLoginUrl = casConfig.loginUrl ?: null
        configuredLogoutUrl = casConfig.logoutUrl ?: null
        servicePath = stripTrailingSlash(casConfig.servicePath ?: "")
        failurePath = casConfig.failurePath ?: null
        loginParameters = casConfig.loginParameters ?: null
    }

    static String getServerUrl() {
        return serverUrl
    }

    static String getSingleSignOutArtifactParameterName() {
        return singleSignOutArtifactParameterName
    }

    static String getSingleSignOutLogoutParameterName() {
        return singleSignOutLogoutParameterName
    }

    static String getServiceUrl() {
        if (servicePath) {
            try {
                def httpRequest = WebUtils.getHttpRequest(SecurityUtils.subject)
                if (httpRequest) {
                    return requestUrlWithAlternatePath(httpRequest, servicePath)
                }
            } catch (UnavailableSecurityManagerException ex) {
                log.debug("Unable to get a dynamic serviceUrl, reverting to default.", ex)
            }
        }
        return defaultServiceUrl
    }

    static String getFailureUrl() {
        if (failurePath) {
            try {
                def httpRequest = WebUtils.getHttpRequest(SecurityUtils.subject)
                if (httpRequest) {
                    return requestUrlWithAlternatePath(httpRequest, failurePath)
                }
            } catch (UnavailableSecurityManagerException ex) {
                log.debug("Unable to get a dynamic failureUrl, reverting to default.", ex)
            }
        }
        return defaultFailureUrl
    }

    private static String requestUrlWithAlternatePath(HttpServletRequest request, String path) {
        return UriComponentsBuilder.fromHttpUrl(request.requestURL.toString()).replacePath(path).build().encode().toUriString()
    }

    static String getDefaultLoginUrl() {
        return assembleLoginUrl(defaultServiceUrl)
    }

    static String getLoginUrl() {
        return assembleLoginUrl(serviceUrl)
    }

    static String getDefaultLogoutUrl() {
        return assembleLogoutUrl(defaultServiceUrl)
    }

    static String getLogoutUrl() {
        return assembleLogoutUrl(serviceUrl)
    }

    private static String assembleLoginUrl(String serviceUrl) {
        def builder
        if (configuredLoginUrl) { // if we have a configuredLoginUrl, then use that.
            builder = UriComponentsBuilder.fromHttpUrl(configuredLoginUrl)
        } else { // if we don't have a configuredLoginUrl, build one from the serverUrl.
            if (!serverUrl) return ""
            builder = UriComponentsBuilder.fromHttpUrl(serverUrl).path("/login").queryParam("service", serviceUrl)
        }
        if (loginParameters) {
            loginParameters.each { name, value ->
                builder.queryParam(name, value)
            }
        }
        return builder.build().encode().toUriString()
    }

    private static String assembleLogoutUrl(String serviceUrl) {
        def builder
        if (configuredLogoutUrl) { // if we have a configuredLogoutUrl, then use that.
            builder = UriComponentsBuilder.fromHttpUrl(configuredLogoutUrl)
        } else { // if we don't have a configuredLogoutUrl, build one from the serverUrl.
            if (!serverUrl) return ""
            builder = UriComponentsBuilder.fromHttpUrl(serverUrl).path("/logout").queryParam("service", serviceUrl)
        }
        return builder.build().encode().toUriString()
    }

    static boolean isSingleSignOutDisabled() {
        return Holders.config.security.shiro.cas.singleSignOut.disabled ?: false
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
