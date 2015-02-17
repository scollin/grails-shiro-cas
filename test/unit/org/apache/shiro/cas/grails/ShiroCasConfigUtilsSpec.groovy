package org.apache.shiro.cas.grails

import grails.util.Holders
import org.apache.commons.logging.Log
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import spock.lang.Specification

class ShiroCasConfigUtilsSpec extends Specification {
    Log realLog
    Log mockLog = Mock(Log)

    LinkGenerator mockLinkGenerator = Mock(LinkGenerator)

    void setup() {
        realLog = ShiroCasConfigUtils.log
        ShiroCasConfigUtils.log = mockLog
        ShiroCasConfigUtils.linkGenerator = mockLinkGenerator
    }

    void cleanup() {
        ShiroCasConfigUtils.log = realLog
        ShiroCasConfigUtils.linkGenerator = null
    }

    void "missing config logs errors"() {
        when: "initialized with no configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.emptyConfiguration)

        then: "config errors are logged"
        1 * mockLog.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
    }

    void "minimal configuration is dynamically determined when the serverUrl is set"() {
        setup:
        mockLinkGenerator.getServerBaseURL() >> "https://app.default.com/default-context/"

        when: "initialized with only the serverUrl set"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.serverUrlOnlyConfiguration)

        then: "no config errors are logged"
        0 * mockLog.error(_)

        and: "the configured server URL is used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://app.default.com/default-context/shiro-cas"
    }

    void "configurations without a baseServiceUrl use dynamic URLs from the application context"(){
        setup:
        2 * mockLinkGenerator.getServerBaseURL() >> "https://dynamic.test.server/ctx/"
        2 * mockLinkGenerator.getServerBaseURL() >> "https://second.test.server/ctx/"

        when: "initialized with a dynamicServerName configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.serverUrlOnlyConfiguration)

        then: "URLs overridden using first domain"
        ShiroCasConfigUtils.serviceUrl == "https://dynamic.test.server/ctx/shiro-cas"
        ShiroCasConfigUtils.failureUrl == "https://dynamic.test.server/ctx/auth/cas-failure"

        and: "URLs overridden using second domain"
        ShiroCasConfigUtils.serviceUrl == "https://second.test.server/ctx/shiro-cas"
        ShiroCasConfigUtils.failureUrl == "https://second.test.server/ctx/auth/cas-failure"
    }

    void "custom service and failure path work with dynamic server base url"(){
        setup:
        2 * mockLinkGenerator.getServerBaseURL() >> "https://dynamic.test.server/ctx/"
        2 * mockLinkGenerator.getServerBaseURL() >> "https://second.test.server/ctx/"

        when: "initialized with a custom dynamic configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.customDynamicConfiguration)

        then: "URLs overridden using first domain"
        ShiroCasConfigUtils.serviceUrl == "https://dynamic.test.server/ctx/cas-callback"
        ShiroCasConfigUtils.failureUrl == "https://dynamic.test.server/ctx/cas-failure"

        and: "URLs overridden using second domain"
        ShiroCasConfigUtils.serviceUrl == "https://second.test.server/ctx/cas-callback"
        ShiroCasConfigUtils.failureUrl == "https://second.test.server/ctx/cas-failure"
    }

    void "user-supplied static configuration works"() {
        when: "initialized with a static configuration"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.staticConfiguration)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values take precedence"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://static.example.com/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/login?service=https://static.example.com/app/shiro-cas"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/logout?service=https://static.example.com/app/shiro-cas"
        ShiroCasConfigUtils.failureUrl == "https://static.example.com/app/auth/cas-failure"
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
        ShiroCasConfigUtils.serviceUrl == "https://localhost:8080/app/cas-callback"
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/custom-login?renew=true"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/custom-logout"
        ShiroCasConfigUtils.failureUrl == "https://localhost:8080/app/cas-failure"
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n/other=otherFilter"
        !ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.singleSignOutArtifactParameterName == "token"
        ShiroCasConfigUtils.singleSignOutLogoutParameterName == "slo"
    }

    void "trailing slashes on URLs are ignored"() {
        when: "initialized with server url and/or service url containing a trailing slash"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.staticConfiguration)

        then: "the trailing slash is ignored"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://static.example.com/app/shiro-cas"
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/login?service=https://static.example.com/app/shiro-cas"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/logout?service=https://static.example.com/app/shiro-cas"
    }

    void "specified login parameters are honored"() {
        setup:
        mockLinkGenerator.getServerBaseURL() >> "https://app.example.com"

        when: "initialized with a configuration including login parameters"
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.configurationWithLoginParameters)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com/cas"
        ShiroCasConfigUtils.serviceUrl == "https://app.example.com/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/cas/login?service=https://app.example.com/shiro-cas&renew=true&gateway=true&welcome=Welcome%20to%20Shiro%20Cas"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/cas/logout?service=https://app.example.com/shiro-cas"
        ShiroCasConfigUtils.failureUrl == "https://app.example.com/auth/cas-failure"
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
}
