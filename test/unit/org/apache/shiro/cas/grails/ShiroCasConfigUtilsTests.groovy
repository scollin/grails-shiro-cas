package org.apache.shiro.cas.grails

class ShiroCasConfigUtilsTests {
    // TODO: test logging
    void testMissingConfig() {
        init("")
        assert ShiroCasConfigUtils.serverUrl == ""
        assert ShiroCasConfigUtils.serviceUrl == ""
        assert ShiroCasConfigUtils.loginUrl == "/login?service="
        assert ShiroCasConfigUtils.logoutUrl == "/logout?service="
        assert ShiroCasConfigUtils.failureUrl == null
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void testMinimalConfig() {
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas"
        """)
        assert ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        assert ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.loginUrl == "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.failureUrl == null
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void testTrailingSlashes() {
        init("""
security.shiro.cas.serverUrl = "https://localhost/cas/"
security.shiro.cas.serviceUrl = "http://localhost:8080/app/shiro-cas/"
        """)
        assert ShiroCasConfigUtils.serverUrl == "https://localhost/cas"
        assert ShiroCasConfigUtils.serviceUrl == "http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.loginUrl == "https://localhost/cas/login?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.logoutUrl == "https://localhost/cas/logout?service=http://localhost:8080/app/shiro-cas"
        assert ShiroCasConfigUtils.failureUrl == null
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n"
    }

    void testFullConfig() {
        init("""
security.shiro.cas.serverUrl = "https://cas.example.com"
security.shiro.cas.serviceUrl = "https://app.example.com/shiro-cas"
security.shiro.cas.loginUrl = "https://cas.example.com/customLogin"
security.shiro.cas.logoutUrl = "https://cas.example.com/customLogout"
security.shiro.cas.failureUrl = "https://app.example.com/casFailure"
security.shiro.filter.filterChainDefinitions = "/other=otherFilter"
        """)
        assert ShiroCasConfigUtils.serverUrl == "https://cas.example.com"
        assert ShiroCasConfigUtils.serviceUrl == "https://app.example.com/shiro-cas"
        assert ShiroCasConfigUtils.loginUrl == "https://cas.example.com/customLogin"
        assert ShiroCasConfigUtils.logoutUrl == "https://cas.example.com/customLogout"
        assert ShiroCasConfigUtils.failureUrl == "https://app.example.com/casFailure"
        assert ShiroCasConfigUtils.shiroCasFilter == "/shiro-cas=casFilter\n/other=otherFilter"
    }

    static void init(String script) {
        def config = new ConfigSlurper().parse(script)
        ShiroCasConfigUtils.initialize(config)
    }
}
