grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.source.level = "1.6"
grails.project.target.level = "1.6"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        // mavenLocal() // generally not a good idea, as it can result in non-reproducible builds
        mavenCentral()
    }
    dependencies {
        compile "org.apache.shiro:shiro-cas:1.2.2"
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }

    plugins {
        compile ":shiro:1.2.0"
        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
        build(":release:2.2.1") {
            export = false
        }
    }
}
