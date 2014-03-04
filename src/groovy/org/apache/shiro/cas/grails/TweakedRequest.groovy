package org.apache.shiro.cas.grails

import groovy.transform.PackageScope

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/**
 * Shiro expects to get the URL to forward to from the request's requestURI; however, Grails mucks with the
 * request in a way that makes that not work... so tweak the request we're passing to Shiro to make it work.
 */
@PackageScope
class TweakedRequest extends HttpServletRequestWrapper {
    private final HttpServletRequest grailsRequest

    TweakedRequest(HttpServletRequest grailsRequest) {
        super(grailsRequest)
        this.grailsRequest = grailsRequest
    }

    @Override
    String getRequestURI() {
        return grailsRequest.getForwardURI()
    }
}
