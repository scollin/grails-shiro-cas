package org.apache.shiro.cas.grails

import groovy.util.logging.Slf4j
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.cas.CasFilter
import org.apache.shiro.web.util.WebUtils

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Slf4j
class DynamicServerNameCasFilter extends CasFilter {
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                     ServletResponse response) {
        def subject = getSubject(request, response)
        if (!(subject.authenticated || subject.remembered)) {
            def failureUrl = ShiroCasConfigUtils.failureUrl
            try {
                WebUtils.issueRedirect(request, response, failureUrl)
            } catch (IOException e) {
                log.error("Cannot redirect to failure url : {}", failureUrl, e)
            }
            return false
        }
        return super.onLoginFailure(token, ae, request, response);
    }
    
    @Override
    public String getLoginUrl() {
        return ShiroCasConfigUtils.loginUrl
    }
}
