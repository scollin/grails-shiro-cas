import grails.spring.BeanBuilder
import org.apache.shiro.cas.CasFilter
import org.apache.shiro.cas.CasSubjectFactory
import org.apache.shiro.cas.grails.ShiroCasConfigUtils
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator

class ShiroCasGrailsPlugin {
    def version = "0.2.0"
    def grailsVersion = "2.0 > *"
    def loadAfter = ["shiro"]
    def title = "Shiro CAS Plugin"
    def description = 'Enables Grails applications to use JASIG CAS for single sign-on with Apache Shiro'
    def documentation = "https://github.com/commercehub-oss/grails-shiro-cas/blob/master/README.md"
    def license = "APACHE"
    def developers = [
        [name: "David M. Carr", email: "dcarr@commercehub.com"],
        [name: "Ford Guo", email: "agile.guo@gmail.com"],
        [name: "Gavin Hogan", email: "gavin@commercehub.com"]
    ]
    def organization = [ name: "CommerceHub", url: "http://www.commercehub.com/" ]
    def issueManagement = [ system: "GitHub", url: "https://github.com/commercehub-oss/grails-shiro-cas/issues" ]
    def scm = [ url: "https://github.com/commercehub-oss/grails-shiro-cas/" ]

    def doWithSpring = {
        def securityConfig = application.config.security.shiro
        def beanBuilder = delegate as BeanBuilder
        ShiroCasConfigUtils.initialize(application.config)
        casTicketValidator(Cas20ServiceTicketValidator, ShiroCasConfigUtils.serverUrl)
        casSubjectFactory(CasSubjectFactory)
        def shiroSecurityManager = beanBuilder.getBeanDefinition("shiroSecurityManager")
        shiroSecurityManager.propertyValues.add("subjectFactory", casSubjectFactory)
        if (!securityConfig.filter.config) {
            casFilter(CasFilter) {bean->
                if (ShiroCasConfigUtils.failureUrl) {
                    failureUrl = ShiroCasConfigUtils.failureUrl
                }
            }
            def shiroFilter = beanBuilder.getBeanDefinition("shiroFilter")
            if (!securityConfig.filter.filterChainDefinitions) {
                shiroFilter.propertyValues.addPropertyValue("filterChainDefinitions", ShiroCasConfigUtils.shiroCasFilter)
            }
            if (!securityConfig.filter.loginUrl) {
                shiroFilter.propertyValues.addPropertyValue("loginUrl", ShiroCasConfigUtils.loginUrl)
            }
        }
    }
}
