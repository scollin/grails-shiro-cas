package org.apache.shiro.cas.grails

import groovy.util.logging.Slf4j
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.cas.CasFilter
import org.apache.shiro.subject.Subject
import org.apache.shiro.web.util.WebUtils

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Slf4j
class DynamicServerNameCasFilter extends CasFilter {

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                     ServletResponse response) {
        
        if(ShiroCasConfigUtils.isServerNameDynamic()) {
            //FailureUrl changes depending on the domain the request was to.
            Subject subject = getSubject(request, response);
            if (subject.isAuthenticated() || subject.isRemembered()) {
                try {
                    issueSuccessRedirect(request, response);
                } catch (Exception e) {
                    log.error("Cannot redirect to the default success url", e);
                }
            } else {
                try {
                    WebUtils.issueRedirect(request, response, ShiroCasConfigUtils.failureUrl);
                } catch (IOException e) {
                    log.error("Cannot redirect to failure url : {}", ShiroCasConfigUtils.failureUrl, e);
                }
            }
        }
        else{
           return super.onLoginFailure(token, ae, request, response)
        }
        
        return false;
    }
    
    @Override
    public String getLoginUrl() {
        return ShiroCasConfigUtils.getLoginUrl()
    }
}
