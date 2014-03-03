import grails.spring.BeanBuilder
import org.apache.shiro.cas.CasFilter
import org.apache.shiro.cas.CasSubjectFactory
import org.apache.shiro.grails.ConfigUtils

class ShiroCasGrailsPlugin {
    // the plugin version
    def version = "0.1.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    def dependsOn = [shiro: "1.2.0 > *"]
    def loadAfter = ["shiro"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Shiro CAS Plugin"
    def author = "David M. Carr"
    def authorEmail = "dcarr@commercehub.com"
    def description = '''\
Enables Grails applications to use JASIG CAS for single sign-on with Apache Shiro.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/shiro-cas"

    // Extra (optional) plugin metadata

    def license = "APACHE"

    def developers = [ [ name: "Ford Guo", email: "agile.guo@gmail.com" ]]

    def organization = [ name: "CommerceHub", url: "http://www.commercehub.com/" ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/commercehub-oss/grails-shiro-cas/issues" ]

    def scm = [ url: "https://github.com/commercehub-oss/grails-shiro-cas/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

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
    // TODO: somehow handle redirect after authentication, previously in accessControlMethod
    // TODO: see saved request hack

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
