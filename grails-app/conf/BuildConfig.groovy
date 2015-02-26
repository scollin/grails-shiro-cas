grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits("global") {
        excludes 'ehcache'
    }
    log 'warn'

    repositories {
        inherit false
        grailsCentral()
        // mavenLocal() // generally not a good idea, as it can result in non-reproducible builds
        mavenCentral()
        mavenRepo "https://repo.grails.org/grails/plugins-releases"
    }

    dependencies {
        compile "org.apache.shiro:shiro-cas:1.2.2"
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0", {
            export = false
        }
    }

    plugins {
        compile ":shiro:1.2.1"
        test(":spock:0.7") {
            exclude "spock-grails-support"
            export = false
        }
        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
