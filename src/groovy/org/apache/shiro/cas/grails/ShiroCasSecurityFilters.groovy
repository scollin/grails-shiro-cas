package org.apache.shiro.cas.grails

import org.apache.shiro.subject.Subject
import org.codehaus.groovy.grails.plugins.web.filters.FilterConfig

abstract class ShiroCasSecurityFilters {
    boolean onNotAuthenticated(Subject subject, FilterConfig filter) {
        def doDefault = false
        ShiroCasUtils.redirectToCasLogin(filter)
        return doDefault
    }
}
