package org.apache.shiro.cas.grails

import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpServletResponse

class ShiroCasUrlBuilder {
    private final UriComponentsBuilder componentsBuilder

    private ShiroCasUrlBuilder(String url) {
        componentsBuilder = UriComponentsBuilder.fromHttpUrl(url)
    }

    String getUrl() {
        return componentsBuilder.build().encode().toUriString()
    }

    void go(HttpServletResponse response) {
        response.sendRedirect(url)
    }

    ShiroCasUrlBuilder withQueryParam(String name, String value) {
        componentsBuilder.queryParam(name, value)
        return this
    }

    ShiroCasUrlBuilder withRenew() {
        return withQueryParam("renew", "true")
    }

    ShiroCasUrlBuilder withGateway() {
        return withQueryParam("gateway", "true")
    }

    static ShiroCasUrlBuilder forLogin() {
        return new ShiroCasUrlBuilder(ShiroCasConfigUtils.getLoginUrl())
    }

    static ShiroCasUrlBuilder forLogout() {
        return new ShiroCasUrlBuilder(ShiroCasConfigUtils.getLogoutUrl())
    }
}
