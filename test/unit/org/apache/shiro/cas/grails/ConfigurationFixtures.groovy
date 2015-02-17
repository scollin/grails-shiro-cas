package org.apache.shiro.cas.grails

class ConfigurationFixtures {
    static final ConfigObject emptyConfiguration = parse("")

    static final ConfigObject serverUrlOnlyConfiguration = parse("""
        |security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        |""".stripMargin())

    static final ConfigObject customDynamicConfiguration = parse("""
        |security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        |security.shiro.cas.servicePath = "/cas-callback"
        |security.shiro.cas.failurePath = "/cas-failure"
        |""".stripMargin())

    static final ConfigObject staticConfiguration = parse("""
        |security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        |security.shiro.cas.baseServiceUrl = "https://static.example.com/app/"
        |""".stripMargin())

    static final ConfigObject fullConfiguration = parse("""
        |security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        |security.shiro.cas.baseServiceUrl = "https://localhost:8080/app/"
        |security.shiro.cas.servicePath = "/cas-callback"
        |security.shiro.cas.failurePath = "/cas-failure"
        |security.shiro.cas.loginUrl = "https://cas.example.com/cas/custom-login"
        |security.shiro.cas.logoutUrl = "https://cas.example.com/cas/custom-logout"
        |security.shiro.cas.loginParameters.renew = true
        |security.shiro.cas.singleSignOut.disabled = false
        |security.shiro.cas.singleSignOut.artifactParameterName = "token"
        |security.shiro.cas.singleSignOut.logoutParameterName = "slo"
        |security.shiro.filter.filterChainDefinitions = "/other=otherFilter"
        |""".stripMargin())

    static final ConfigObject configurationWithLoginParameters = parse("""
        |security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        |security.shiro.cas.loginParameters.renew = true
        |security.shiro.cas.loginParameters.gateway = true
        |security.shiro.cas.loginParameters.welcome = "Welcome to Shiro Cas"
        |""".stripMargin())

    static final ConfigObject configurationWithSingleSignOutDisabled = parse("""
        |security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        |security.shiro.cas.singleSignOut.disabled = true
        |""".stripMargin())

    private static ConfigObject parse(String script) {
        return new ConfigSlurper().parse(script)
    }
}
