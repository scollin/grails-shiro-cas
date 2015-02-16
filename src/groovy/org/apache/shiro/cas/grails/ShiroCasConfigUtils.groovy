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
    private static String configuredLoginUrl
    private static String configuredLogoutUrl
    private static String configuredBaseServiceUrl
    private static String servicePath
    private static String failurePath
    private static Map loginParameters
    private static String singleSignOutArtifactParameterName
    private static String singleSignOutLogoutParameterName
    private static String filterChainDefinitions

    static void initialize(ConfigObject config) {
        processRequiredConfiguration(config)

        if (minimalConfigurationValid()) {
            processOptionalConfiguration(config)
        }
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
        return baseServiceUrl + servicePath
    }

    static String getFailureUrl() {
        return failurePath ? baseServiceUrl + failurePath : ""
    }

    static String getLoginUrl() {
        return assembleLoginUrl(serviceUrl)
    }

    static String getLogoutUrl() {
        return assembleLogoutUrl(serviceUrl)
    }

    static private void processRequiredConfiguration(ConfigObject config) {
        serverUrl = stripTrailingSlash(config.security.shiro.cas.serverUrl ?: "")
        configuredBaseServiceUrl = stripTrailingSlash(config.security.shiro.cas.baseServiceUrl ?: "")
    }

    static private void processOptionalConfiguration(ConfigObject config) {
        servicePath = config.security.shiro.cas.servicePath ?: "/shiro-cas"
        singleSignOutArtifactParameterName = config.security.shiro.cas.singleSignOut.artifactParameterName ?: "ticket"
        singleSignOutLogoutParameterName = config.security.shiro.cas.singleSignOut.logoutParameterName ?: "logoutRequest"
        filterChainDefinitions = config.security.shiro.filter.filterChainDefinitions ?: ""
        loginParameters = config.security.shiro.cas.loginParameters ?: null
        configuredLoginUrl = config.security.shiro.cas.loginUrl ?: null
        configuredLogoutUrl = config.security.shiro.cas.logoutUrl ?: null

        failurePath = config.security.shiro.cas.failurePath ?: ""
    }

    static private boolean minimalConfigurationValid() {
        if (!serverUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        }

        return serverUrl
    }

    private static String getBaseServiceUrl() {
        return configuredBaseServiceUrl ?: defaultBaseServiceUrl
    }

    private static String getDefaultBaseServiceUrl() {
        return stripTrailingSlash(Holders.grailsApplication?.mainContext?.getBean('grailsLinkGenerator')?.serverBaseURL)
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
