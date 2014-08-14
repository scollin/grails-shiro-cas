package org.apache.shiro.cas.grails

import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@SuppressWarnings("GrMethodMayBeStatic")
class ShiroCasUrlBuilderSpec extends Specification {
    def setup() {
        init("""
security.shiro.cas.serverUrl = "https://cas.example.com"
security.shiro.cas.serviceUrl = "https://app.example.com/shiro-cas"
        """)
    }

    def "can build from login"() {
        expect:
        ShiroCasUrlBuilder.forLogin().url == "https://cas.example.com/login?service=https://app.example.com/shiro-cas"
        ShiroCasUrlBuilder.forLogin().withGateway().url == "https://cas.example.com/login?service=https://app.example.com/shiro-cas&gateway=true"
        ShiroCasUrlBuilder.forLogin().withRenew().url == "https://cas.example.com/login?service=https://app.example.com/shiro-cas&renew=true"
        ShiroCasUrlBuilder.forLogin().withQueryParam("token", "12345").url == "https://cas.example.com/login?service=https://app.example.com/shiro-cas&token=12345"
    }

    def "can build from logout"() {
        expect:
        ShiroCasUrlBuilder.forLogout().url == "https://cas.example.com/logout?service=https://app.example.com/shiro-cas"
        ShiroCasUrlBuilder.forLogout().withQueryParam("token", "12345").url == "https://cas.example.com/logout?service=https://app.example.com/shiro-cas&token=12345"
    }

    def "can redirect"() {
        def response = Mock(HttpServletResponse)

        when:
        ShiroCasUrlBuilder.forLogin().go(response)

        then:
        1 * response.sendRedirect("https://cas.example.com/login?service=https://app.example.com/shiro-cas")

        when:
        ShiroCasUrlBuilder.forLogin().withRenew().withQueryParam("token", "12345").go(response)

        then:
        1 * response.sendRedirect("https://cas.example.com/login?service=https://app.example.com/shiro-cas&renew=true&token=12345")
    }

    def "query parameters are encoded"() {
        expect:
        ShiroCasUrlBuilder.forLogin().withQueryParam("message", "Welcome to CAS").url == "https://cas.example.com/login?service=https://app.example.com/shiro-cas&message=Welcome%20to%20CAS"
    }

    static void init(String script) {
        def config = new ConfigSlurper().parse(script)
        ShiroCasConfigUtils.initialize(config)
    }
}
