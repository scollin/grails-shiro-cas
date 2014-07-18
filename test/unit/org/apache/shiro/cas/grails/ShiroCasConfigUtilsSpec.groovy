package org.apache.shiro.cas.grails

import org.apache.commons.logging.Log
import spock.lang.Specification

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
        assert ShiroCasConfigUtils.serverUrl == ""
        assert ShiroCasConfigUtils.serviceUrl == ""
        assert ShiroCasConfigUtils.loginUrl == "/login?service="
        assert ShiroCasConfigUtils.logoutUrl == "/logout?service="
        assert ShiroCasConfigUtils.failureUrl == null
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void testMinimalConfig() {
        when: "initialized with a minimal configuration"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
        """)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        assert ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        assert ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        assert ShiroCasConfigUtils.loginUrl == "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.failureUrl == null
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void testTrailingSlashes() {
        when: "initialized with server url and/or service url containing a trailing slash"
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas/"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas/"
        """)

        then: "the trailing slash is ignored"
        assert ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        assert ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.loginUrl == "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
    }

    void testFullConfig() {
        when: "initalized with a full config"
        init("""
security.shiro.cas.serverUrl = "https://cas.example.com"
security.shiro.cas.serviceUrl = "https://app.example.com/shiro-cas"
security.shiro.cas.loginUrl = "https://cas.example.com/customLogin"
security.shiro.cas.logoutUrl = "https://cas.example.com/customLogout"
security.shiro.cas.failureUrl = "https://app.example.com/casFailure"
security.shiro.filter.filterChainDefinitions = "/other=otherFilter"
security.shiro.cas.loginParameters.renew = true

        """)

        then: "no errors are logged"
        0 * mockLog.error(_)

        and: "the configured values are used"
        assert ShiroCasConfigUtils.serverUrl == "https://cas.example.com"
        assert ShiroCasConfigUtils.serviceUrl == "https://app.example.com/shiro-cas"
        assert ShiroCasConfigUtils.loginUrl == "https://cas.example.com/customLogin?renew=true"
        assert ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/customLogout"
        assert ShiroCasConfigUtils.failureUrl == "https://app.example.com/casFailure"
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n/other=otherFilter"
    }


    void testLoginParamsMinimalConfig() {
        when: "initialized with a minimal configuration"
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
        assert ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        assert ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"

        and: "other values are either defaulted or based on the configured values"
        assert ShiroCasConfigUtils.loginUrl ==
                "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas&renew=true&gateway=true&welcome=Welcome+to+Shiro+Cas"
        assert ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.failureUrl == null
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    static void init(String script) {
        def config = new ConfigSlurper().parse(script)
        ShiroCasConfigUtils.initialize(config)
    }
}
