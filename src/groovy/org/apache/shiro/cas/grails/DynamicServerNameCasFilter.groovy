package org.apache.shiro.cas.grails

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.cas.CasFilter
import org.apache.shiro.subject.Subject
import org.apache.shiro.web.util.WebUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class DynamicServerNameCasFilter extends CasFilter {

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                     ServletResponse response) {

        Logger logger = LoggerFactory.getLogger(CasFilter.class);
        
        //FailureUrl changes depending on the domain the request was to.
        Subject subject = getSubject(request, response);
        if (subject.isAuthenticated() || subject.isRemembered()) {
            try {
                issueSuccessRedirect(request, response);
            } catch (Exception e) {
                logger.error("Cannot redirect to the default success url", e);
            }
        } else {
            try {
                WebUtils.issueRedirect(request, response, ShiroCasConfigUtils.failureUrl);
            } catch (IOException e) {
                logger.error("Cannot redirect to failure url : {}", ShiroCasConfigUtils.failureUrl, e);
            }
        }
        
        return false;
    }
    
    @Override
    public String getLoginUrl() {
        return ShiroCasConfigUtils.loginUrl
    }
}
