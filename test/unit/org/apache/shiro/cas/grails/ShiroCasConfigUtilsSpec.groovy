package org.apache.shiro.cas.grails

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.shiro.SecurityUtils
import org.apache.shiro.web.util.WebUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.ApplicationContext
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class ShiroCasConfigUtilsSpec extends Specification {
    Log realLog
    Log mockLog = Mock(Log)

    def mockLinkGenerator = GroovyMock(Object)

    void setup() {
        realLog = ShiroCasConfigUtils.log
        ShiroCasConfigUtils.log = mockLog

        def mockContext = GroovyMock(ApplicationContext) {
            getBean('grailsLinkGenerator') >> mockLinkGenerator
        }

        Holders.grailsApplication = GroovyMock(GrailsApplication) {
            getMainContext() >> mockContext
        }
    }

    void cleanup() {
        ShiroCasConfigUtils.log = realLog
    }

    void "missing config logs errors"() {
        when: "initialized with no configuration"
        ShiroCasConfigUtils.initialize(new ConfigObject())

        then: "config errors are logged"
        1 * mockLog.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
    }

    void "minimal configuration is dynamically determined when the serverUrl is set"() {
        setup:
        (1.._) * mockLinkGenerator.getServerBaseURL() >> "https://app.default.com/default-context/"

        when: "initialized with only the serverUrl set"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.serverUrlOnlyConfiguration)

        then:
        0 * mockLog.error(_)

        and: "the configured server URL is used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://app.default.com/default-context/shiro-cas"
        ShiroCasConfigUtils.servicePath == "/shiro-cas"
    }

    void "user-supplied minimal configuration works"() {
        when: "initialized with a minimal configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.minimalConfiguration)

        then: "no errors are logged"
        0 * mockLog.error(_)
        0 * mockLinkGenerator.getServerBaseURL()

        and: "the configured values take precedence"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
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
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/customLogin?renew=true"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/customLogout"
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
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
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
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&renew=true&gateway=true&welcome=Welcome%20to%20Shiro%20Cas"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.failureUrl == ""
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n"
    }

    void "single sign out support can be disabled"() {
        when: "initialized with a configuration including login parameters"
        def config = ConfigurationFixtures.configurationWithSingleSignOutDisabled
        Holders.config = config
        ShiroCasConfigUtils.initialize(config)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "single sign out support is disabled"
        ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void "configurations without a baseServiceUrl use dynamic URLs from the application context"(){
        setup:
        mockLinkGenerator.getServerBaseURL() >> "https://dynamic.test.server/ctx/"

        when: "initialized with a dynamicServerName configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.configurationWithDynamicServerName)

        then: "URLs overridden using first domain"
        ShiroCasConfigUtils.serviceUrl == "https://dynamic.test.server/ctx/shiro-cas"
        ShiroCasConfigUtils.failureUrl == "https://dynamic.test.server/ctx/casFailure"
        def firstFailureUrl = ShiroCasConfigUtils.failureUrl
    }
}
