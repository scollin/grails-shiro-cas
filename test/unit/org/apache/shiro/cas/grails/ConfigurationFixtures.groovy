package org.apache.shiro.cas.grails

class ConfigurationFixtures {
    static ConfigObject getServerUrlOnlyConfiguration() {
        return new ConfigSlurper().parse("""
security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
        """)
    }

    static ConfigObject getMinimalConfiguration() {
        return new ConfigSlurper().parse("""
security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
security.shiro.cas.baseServiceUrl = "https://localhost:8080/app/"
security.shiro.cas.servicePath = "/shiro-cas"
        """)
    }

    static ConfigObject getFullConfiguration() {
        return new ConfigSlurper().parse("""
security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
security.shiro.cas.baseServiceUrl = "https://localhost:8080/app/"
security.shiro.cas.servicePath = "/shiro-cas"
security.shiro.cas.loginUrl = "https://cas.example.com/cas/customLogin"
security.shiro.cas.logoutUrl = "https://cas.example.com/cas/customLogout"
security.shiro.cas.failurePath = "/casFailure"
security.shiro.filter.filterChainDefinitions = "/other=otherFilter"
security.shiro.cas.loginParameters.renew = true
security.shiro.cas.singleSignOut.disabled = false
security.shiro.cas.singleSignOut.artifactParameterName = "token"
security.shiro.cas.singleSignOut.logoutParameterName = "slo"
        """)
    }

    static ConfigObject getConfigurationWithLoginParameters() {
        return new ConfigSlurper().parse("""
security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
security.shiro.cas.baseServiceUrl = "https://localhost:8080/app/"
security.shiro.cas.servicePath = "/shiro-cas"
security.shiro.cas.loginParameters.renew = true
security.shiro.cas.loginParameters.gateway = true
security.shiro.cas.loginParameters.welcome = "Welcome to Shiro Cas"
        """)
    }

    static ConfigObject getConfigurationWithSingleSignOutDisabled() {
        return new ConfigSlurper().parse("""
security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
security.shiro.cas.baseServiceUrl = "https://localhost:8080/app/"
security.shiro.cas.servicePath = "/shiro-cas"
security.shiro.cas.singleSignOut.disabled = true
        """)
    }

    static ConfigObject getConfigurationWithDynamicServerName() {
        return new ConfigSlurper().parse("""
security.shiro.cas.serverUrl = "https://cas.example.com/cas/"
security.shiro.cas.servicePath = "/shiro-cas"
security.shiro.cas.failurePath = "/casFailure"
        """)
    }
}