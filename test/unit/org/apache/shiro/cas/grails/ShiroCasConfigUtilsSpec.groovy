package org.apache.shiro.cas.grails

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.web.util.WebUtils
import org.springframework.http.HttpRequest
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
        init("")

        then: "config errors are logged"
        1 * mockLog.error("Invalid application configuration: security.shiro.cas.serverUrl is required; it should be https://host:port/cas")
        1 * mockLog.error("Invalid application configuration: security.shiro.cas.serviceUrl is required; it should be http://host:port/mycontextpath/shiro-cas")

        and: "a default (but non-working) configuration is used"
        ShiroCasConfigUtils.serverUrl == ""
        ShiroCasConfigUtils.serviceUrl == ""
        ShiroCasConfigUtils.loginUrl == ""
        ShiroCasConfigUtils.logoutUrl == ""
        ShiroCasConfigUtils.failureUrl == null
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n"
        !ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.singleSignOutArtifactParameterName == "ticket"
        ShiroCasConfigUtils.singleSignOutLogoutParameterName == "logoutRequest"
    }

    void "minimal configuration works"() {
        when: "initialized with a minimal configuration"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
        """)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.loginUrl == "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.failureUrl == null
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n"
        !ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.singleSignOutArtifactParameterName == "ticket"
        ShiroCasConfigUtils.singleSignOutLogoutParameterName == "logoutRequest"
    }

    void "full configuration works"() {
        when: "initalized with a full config"
        init("""
security.shiro.cas.serverUrl = "https://cas.example.com"
security.shiro.cas.serviceUrl = "https://app.example.com/shiro-cas"
security.shiro.cas.loginUrl = "https://cas.example.com/customLogin"
security.shiro.cas.logoutUrl = "https://cas.example.com/customLogout"
security.shiro.cas.failureUrl = "https://app.example.com/casFailure"
security.shiro.filter.filterChainDefinitions = "/other=otherFilter"
security.shiro.filter.dynamicServerName = false
security.shiro.cas.loginParameters.renew = true
security.shiro.cas.singleSignOut.disabled = false
security.shiro.cas.singleSignOut.artifactParameterName = "token"
security.shiro.cas.singleSignOut.logoutParameterName = "slo"

        """)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://cas.example.com"
        ShiroCasConfigUtils.serviceUrl == "https://app.example.com/shiro-cas"
        ShiroCasConfigUtils.loginUrl == "https://cas.example.com/customLogin?renew=true"
        ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/customLogout"
        ShiroCasConfigUtils.failureUrl == "https://app.example.com/casFailure"
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n/other=otherFilter"
        !ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.singleSignOutArtifactParameterName == "token"
        ShiroCasConfigUtils.singleSignOutLogoutParameterName == "slo"
    }

    void "trailing slashes on URLs are ignored"() {
        when: "initialized with server url and/or service url containing a trailing slash"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas/"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas/"
        """)

        then: "the trailing slash is ignored"
        ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.loginUrl == "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
    }

    void "specified login parameters are honored"() {
        when: "initialized with a configuration including login parameters"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
security.shiro.cas.loginParameters.renew = true
security.shiro.cas.loginParameters.gateway = true
security.shiro.cas.loginParameters.welcome = "Welcome to Shiro Cas"
        """)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        ShiroCasConfigUtils.loginUrl ==
                "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas&renew=true&gateway=true&welcome=Welcome%20to%20Shiro%20Cas"
        ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
        ShiroCasConfigUtils.failureUrl == null
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=singleSignOutFilter,casFilter\n"
    }

    void "single sign out support can be disabled"() {
        when: "initialized with a configuration including login parameters"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
security.shiro.cas.singleSignOut.disabled = true
        """)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "single sign out support is disabled"
        ShiroCasConfigUtils.singleSignOutDisabled
        ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void "dynamicServerName option handles multiple server names"(){
        setup:
        def firstDomain = "test.server.com"
        def secondDomain = "anothertest.server.com"

        GroovyMock(SecurityUtils, global: true)
        GroovyMock(WebUtils, global: true)
        def httpServletRequest = Mock(HttpServletRequest)
        httpServletRequest.getScheme() >> "http"
        httpServletRequest.getServerPort() >> 80
        WebUtils.getHttpRequest(_) >> httpServletRequest


        when: "initialized with a dynamicServerName configuration"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
security.shiro.cas.serviceUri = "/test/shiro-cas"
security.shiro.cas.failureUri = "/test/"
security.shiro.cas.dynamicServerName = true
        """)

        def firstServiceUrl = ShiroCasConfigUtils.serviceUrl
        def firstFailureUrl = ShiroCasConfigUtils.failureUrl
        def secondServiceUrl = ShiroCasConfigUtils.serviceUrl
        def secondFailureUrl = ShiroCasConfigUtils.failureUrl

        then: "URLs overwridden using first domain"
        2 * httpServletRequest.getServerName() >> firstDomain
        firstServiceUrl == "http://" + firstDomain + "/test/shiro-cas"
        firstFailureUrl == "http://" + firstDomain + "/test/"


        then: "URLs overwridden using second domain"
        2 * httpServletRequest.getServerName() >> secondDomain
        secondServiceUrl == "http://" + secondDomain + "/test/shiro-cas"
        secondFailureUrl == "http://" + secondDomain + "/test/"
    }

    void "dynamicServerName option requires serviceUri"(){

        when: "initialized with an invalid dynamicServerName configuration (without serviceUri)"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
security.shiro.cas.dynamicServerName = true
        """)

        then: "throws an error"
        1 * mockLog.error(_)
    }

    static void init(String script) {
        def config = new ConfigSlurper().parse(script)
        Holders.config = config
        ShiroCasConfigUtils.initialize(config)
    }
}
