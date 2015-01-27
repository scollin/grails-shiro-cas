package org.apache.shiro.cas.grails

import org.apache.shiro.SecurityUtils
import org.apache.shiro.web.util.WebUtils
import org.codehaus.groovy.grails.plugins.web.filters.FilterConfig

final class ShiroCasUtils {
    private ShiroCasUtils() {}

    static void redirectToCasLogin(FilterConfig filter) {
        WebUtils.saveRequest(new TweakedRequest(filter.request))
        filter.redirect(uri: ShiroCasConfigUtils.getLoginUrl())
    }

    /**
     * Logs the user out of the system and returns the appropriate URI for redirection
     *
     * @param defaultLogoutUrl the URL that the user should be redirected to by default
     *
     * @return the URI to which the user should be redirected
     */
    static String logout(String defaultLogoutUrl = "/") {
        def subject = SecurityUtils.subject
        def principal = subject?.principal
        subject?.logout()
        def destinationUrl = ShiroCasPrincipalManager.isFromCas(principal) ? ShiroCasConfigUtils.getLogoutUrl() : defaultLogoutUrl
        ShiroCasPrincipalManager.forgetPrincipal(principal)
        return destinationUrl
    }
}
