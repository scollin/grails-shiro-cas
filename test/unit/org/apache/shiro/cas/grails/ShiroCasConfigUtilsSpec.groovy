package org.apache.shiro.cas.grails

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.shiro.SecurityUtils
import org.apache.shiro.web.util.WebUtils
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class ShiroCasConfigUtilsSpec extends Specification {
    Log realLog
    Log mockLog = Mock(Log)

    void setup() {
        realLog = ShiroCasConfigUtils.log
        ShiroCasConfigUtils.log = mockLog
    }

    void cleanup() {
        ShiroCasConfigUtils.log = realLog
    }

    void "missing config logs errors"() {
        when: "initialized with no configuration"
        ShiroCasConfigUtils.initialize(new ConfigObject())

        then: "config errors are logged"
        1 * mockLog.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        1 * mockLog.error('Invalid application configuration: security.shiro.cas.servicePath is required; it should be /mycontextpath/shiro-cas')
        1 * mockLog.error('Invalid application configuration: security.shiro.cas.baseServiceUrl is required; it should be http://host:port/')

        and: "a default (but non-working) configuration is used"
        ShiroCasConfigUtils.serverUrl == ""
        ShiroCasConfigUtils.serviceUrl == ""
        ShiroCasConfigUtils.defaultLoginUrl == ""
        ShiroCasConfigUtils.defaultLogoutUrl == ""
        ShiroCasConfigUtils.failureUrl == ""
    }

    void "minimal configuration works"() {
        when: "initialized with a minimal configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.minimalConfiguration)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.defaultLoginUrl == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.defaultLogoutUrl == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.failureUrl == ""
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n"
        !ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.singleSignOutArtifactParameterName == "ticket"
        ShiroCasConfigUtils.singleSignOutLogoutParameterName == "logoutRequest"
    }

    void "full configuration works"() {
        when: "initalized with a full config"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.fullConfiguration)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.defaultLoginUrl == "https://cas.example.com/cas/customLogin?renew=true"
        ShiroCasConfigUtils.defaultLogoutUrl == "https://cas.example.com/cas/customLogout"
        ShiroCasConfigUtils.failureUrl == "https://localhost:8080/app/casFailure"
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n/other=otherFilter"
        !ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.singleSignOutArtifactParameterName == "token"
        ShiroCasConfigUtils.singleSignOutLogoutParameterName == "slo"
    }

    void "trailing slashes on URLs are ignored"() {
        when: "initialized with server url and/or service url containing a trailing slash"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.minimalConfiguration)

        then: "the trailing slash is ignored"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.defaultLoginUrl == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.defaultLogoutUrl == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
    }

    void "specified login parameters are honored"() {
        when: "initialized with a configuration including login parameters"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.configurationWithLoginParameters)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.defaultLoginUrl == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&renew=true&gateway=true&welcome=Welcome%20to%20Shiro%20Cas"
        ShiroCasConfigUtils.defaultLogoutUrl == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.failureUrl == ""
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n"
    }

    void "single sign out support can be disabled"() {
        when: "initialized with a configuration including login parameters"
        def config = ConfigurationFixtures.configurationWithSingleSignOnDisabled
        Holders.config = config
        ShiroCasConfigUtils.initialize(config)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "single sign out support is disabled"
        ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void "enabling isServerNameDynamic handles multiple server names"(){
        setup:
        def firstUrl = "http://test.server.com"
        def secondUrl = "http://anothertest.server.com"

        GroovyMock(SecurityUtils, global: true)
        GroovyMock(WebUtils, global: true)
        def httpServletRequest = Mock(HttpServletRequest)
        WebUtils.getHttpRequest(_) >> httpServletRequest


        when: "initialized with a dynamicServerName configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.configurationWithDynamicServerName)

        def firstServiceUrl = ShiroCasConfigUtils.serviceUrl
        def firstFailureUrl = ShiroCasConfigUtils.failureUrl
        def secondServiceUrl = ShiroCasConfigUtils.serviceUrl
        def secondFailureUrl = ShiroCasConfigUtils.failureUrl

        then: "URLs overridden using first domain"
        2 * httpServletRequest.getRequestURL() >> new StringBuffer(firstUrl)
        firstServiceUrl == firstUrl + "/app/shiro-cas"
        firstFailureUrl == firstUrl + "/app/casFailure"

        then: "URLs overridden using second domain"
        2 * httpServletRequest.getRequestURL() >> new StringBuffer(secondUrl)
        secondServiceUrl == secondUrl + "/app/shiro-cas"
        secondFailureUrl == secondUrl + "/app/casFailure"
    }
}
