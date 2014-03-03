class ShiroCasGrailsPlugin {
    // the plugin version
    def version = "0.1.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
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

    def organization = [ name: "CommerceHub", url: "http://www.commercehub.com/" ]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/commercehub-oss/grails-shiro-cas/issues" ]

    def scm = [ url: "https://github.com/commercehub-oss/grails-shiro-cas/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

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
