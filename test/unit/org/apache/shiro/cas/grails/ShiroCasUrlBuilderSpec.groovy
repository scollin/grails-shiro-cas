package org.apache.shiro.cas.grails

import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

class ShiroCasUrlBuilderSpec extends Specification {
    def setup() {
        ShiroCasConfigUtils.initialize(ConfigurationFixtures.minimalConfiguration)
    }

    def "can build from login"() {
        expect:
        ShiroCasUrlBuilder.forLogin().url == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas"
        ShiroCasUrlBuilder.forLogin().withGateway().url == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&gateway=true"
        ShiroCasUrlBuilder.forLogin().withRenew().url == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&renew=true"
        ShiroCasUrlBuilder.forLogin().withQueryParam("token", "12345").url == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&token=12345"
    }

    def "can build from logout"() {
        expect:
        ShiroCasUrlBuilder.forLogout().url == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas"
        ShiroCasUrlBuilder.forLogout().withQueryParam("token", "12345").url == "https://cas.example.com/cas/logout?service=https://localhost:8080/app/shiro-cas&token=12345"
    }

    def "can redirect"() {
        def response = Mock(HttpServletResponse)

        when:
        ShiroCasUrlBuilder.forLogin().go(response)

        then:
        1 * response.sendRedirect("https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas")

        when:
        ShiroCasUrlBuilder.forLogin().withRenew().withQueryParam("token", "12345").go(response)

        then:
        1 * response.sendRedirect("https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&renew=true&token=12345")
    }

    def "query parameters are encoded"() {
        expect:
        ShiroCasUrlBuilder.forLogin().withQueryParam("message", "Welcome to CAS").url == "https://cas.example.com/cas/login?service=https://localhost:8080/app/shiro-cas&message=Welcome%20to%20CAS"
    }
}
