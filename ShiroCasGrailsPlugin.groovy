import grails.spring.BeanBuilder
import org.apache.shiro.cas.CasFilter
import org.apache.shiro.cas.CasSubjectFactory
import org.apache.shiro.cas.grails.ConfigUtils

class ShiroCasGrailsPlugin {
    def version = "0.1.0-SNAPSHOT"
    def grailsVersion = "2.2 > *"
    def dependsOn = [shiro: "1.2.0 > *"]
    def loadAfter = ["shiro"]
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Shiro CAS Plugin"
    def author = "David M. Carr"
    def authorEmail = "dcarr@commercehub.com"
    def description = '''\
Enables Grails applications to use JASIG CAS for single sign-on with Apache Shiro.
'''

    def documentation = "http://grails.org/plugin/shiro-cas"

    def license = "APACHE"

    def developers = [ [ name: "Ford Guo", email: "agile.guo@gmail.com" ]]

    def organization = [ name: "CommerceHub", url: "http://www.commercehub.com/" ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/commercehub-oss/grails-shiro-cas/issues" ]

    def scm = [ url: "https://github.com/commercehub-oss/grails-shiro-cas/" ]

    def doWithSpring = {
        def securityConfig = application.config.security.shiro
        def beanBuilder = delegate as BeanBuilder
        casSubjectFactory(CasSubjectFactory)
        def shiroSecurityManager = beanBuilder.getBeanDefinition("shiroSecurityManager")
        shiroSecurityManager.propertyValues.add("subjectFactory", casSubjectFactory)
        if (!securityConfig.filter.config) {
            casFilter(CasFilter) {bean->
                if (securityConfig.cas.failureUrl) {
                    failureUrl = securityConfig.cas.failureUrl
                }
            }
            def shiroFilter = beanBuilder.getBeanDefinition("shiroFilter")
            if (!securityConfig.filter.filterChainDefinitions) {
                shiroFilter.propertyValues.addPropertyValue("filterChainDefinitions", ConfigUtils.shiroCasFilter)
            }
            if (!securityConfig.filter.loginUrl) {
                shiroFilter.propertyValues.addPropertyValue("loginUrl", ConfigUtils.loginUrl)
            }
        }
    }
}
