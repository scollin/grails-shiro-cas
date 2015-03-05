package org.apache.shiro.cas.grails

import grails.util.Holders
import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.shiro.SecurityUtils
import org.apache.shiro.web.util.WebUtils
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.web.util.UriComponentsBuilder

class ShiroCasConfigUtils {
    @PackageScope
    static Log log = LogFactory.getLog(ShiroCasConfigUtils)

    @PackageScope
    static LinkGenerator linkGenerator = null

    private static String serverUrl
    private static String configuredLoginUrl
    private static String configuredLogoutUrl
    private static String configuredBaseServiceUrl
    private static String servicePath
    private static String failurePath
    private static Map loginParameters
    private static boolean singleSignOutDisabled
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

    static String getValidationUrl() {
        return configuredBaseServiceUrl ? configuredBaseServiceUrl + servicePath : currentUrl
    }

    private static String getCurrentUrl() {
        return WebUtils.getHttpRequest(SecurityUtils.subject)?.requestURL
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

    private static void processRequiredConfiguration(ConfigObject config) {
        serverUrl = stripTrailingSlash(config.security.shiro.cas.serverUrl ?: "")
        configuredBaseServiceUrl = stripTrailingSlash(config.security.shiro.cas.baseServiceUrl ?: "")
    }

    private static void processOptionalConfiguration(ConfigObject config) {
        servicePath = config.security.shiro.cas.servicePath ?: "/shiro-cas"
        singleSignOutDisabled = config.security.shiro.cas.singleSignOut.disabled
        singleSignOutArtifactParameterName = config.security.shiro.cas.singleSignOut.artifactParameterName ?: "ticket"
        singleSignOutLogoutParameterName = config.security.shiro.cas.singleSignOut.logoutParameterName ?: "logoutRequest"
        filterChainDefinitions = config.security.shiro.filter.filterChainDefinitions ?: ""
        loginParameters = config.security.shiro.cas.loginParameters ?: null
        configuredLoginUrl = config.security.shiro.cas.loginUrl ?: null
        configuredLogoutUrl = config.security.shiro.cas.logoutUrl ?: null
        failurePath = config.security.shiro.cas.failurePath ?: "/auth/cas-failure"
    }

    private static boolean minimalConfigurationValid() {
        if (!serverUrl) {
            log.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        }
        return serverUrl
    }

    private static String getBaseServiceUrl() {
        return configuredBaseServiceUrl ?: defaultBaseServiceUrl
    }

    private static String getDefaultBaseServiceUrl() {
        def linkGenerator = linkGenerator ?: Holders.grailsApplication?.mainContext?.getBean('grailsLinkGenerator') as LinkGenerator
        return stripTrailingSlash(linkGenerator?.serverBaseURL)
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
        return singleSignOutDisabled
    }

    static String getShiroCasFilter() {
        def filters = new StringBuilder(servicePath)
        if (!isSingleSignOutDisabled()) {
            // The SingleSignOutFilter must come before the CAS filter
            // https://wiki.jasig.org/display/CASC/Configuring+Single+Sign+Out
            filters.append("=singleSignOutFilter,casFilter\n")
        } else {
            filters.append("=casFilter\n")
        }
        filters.append(filterChainDefinitions)
        return filters.toString()
    }

    private static String stripTrailingSlash(String url) {
        return url?.endsWith("/") ? url[0..-2] : url
    }
}
