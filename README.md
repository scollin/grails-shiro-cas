# Shiro CAS Grails Plugin

[Shiro](http://shiro.apache.org/) is a flexible authentication and authorization framework for Java that has a corresponding [Grails plugin](http://grails.org/plugin/shiro) for simplifying access control in Grails applications. This plugin extends the base Shiro plugin with support for authentication using the [Shiro CAS integration](https://shiro.apache.org/cas.html).

[![Build Status](https://travis-ci.org/commercehub-oss/grails-shiro-cas.png?branch=master)](https://travis-ci.org/commercehub-oss/grails-shiro-cas)

# Usage

This assumes that you've already configured the [Shiro plugin](http://grails.org/plugin/shiro).

To install the plugin, add declaration to your `BuildConfig.groovy` plugins section in the form `compile ":shiro-cas:VERSION"`.

Next, you need to re-configure your `ShiroSecurityFilters`.  The easiest way to accomplish this is to make your class extend `org.apache.shiro.cas.grails.ShiroCasSecurityFilters`.  If that's not an option, you can copy the `onNotAuthenticated` handler into your class.

Finally, you need a CAS-enabled Shiro realm.  Run `grails create-cas-realm` to create such a realm based on a template.  If desired, you can customize the name by specifying `--prefix=PACKAGE.` or `--prefix=PACKAGE.CLASSPREFIX`.  Modify the generated class as needed for your application.

# Configuration

* `security.shiro.cas.serverUrl` (REQUIRED): The URL of the CAS instance to authenticate against.  This should be an HTTPS URL.
* `security.shiro.cas.serviceUrl` (REQUIRED): The URL to pass to CAS as the `service` parameter (see [CAS Protocol](http://www.jasig.org/cas/protocol) for more details on how this is used).  This should be the URL at which end-users can reach the `/shiro-cas` path within the current application (which is automatically registered by this plugin).
* `security.shiro.cas.failureUrl` (RECOMMENDED): The URL that users are redirected to if ticket validation fails.  If this is not specified, ticket validation failures will result in `NullPointerException`s being thrown.
* `security.shiro.cas.loginUrl` (OPTIONAL): The URL that users are redirected to when login is required.  By default, this directs users to `/login` within the `serverUrl`, passing along the service as a query parameter.
* `security.shiro.cas.logoutUrl` (OPTIONAL): The URL that users are redirected to when logging out.  By default, this directs users to `/logout` within the `serverUrl`, passing along the service as a query parameter.
* `security.shiro.cas.loginParameters` (OPTIONAL): Key, Value pairs to be added to the generated or explicitly set loginUrl. (see [CAS Parameters](http://www.jasig.org/cas/protocol#parameters) for more details on how this is used) 
* `security.shiro.cas.singleSignOut.disabled` (OPTIONAL): Boolean value controlling whether to disable Single Sign Out.  By default, this is `false`, resulting in Single Sign Out support being enabled (matching the default for CAS).  Note that this configuration value is used at build-time to modify the `web.xml`, and externalized configuration will not be taken into account during that phase.
* `security.shiro.cas.singleSignOut.artifactParameterName` (OPTIONAL): The parameter used to detect sessions in preparation for Single Sign Out support.  By default, this is `ticket` (matching the default for CAS).
* `security.shiro.cas.singleSignOut.logoutParameterName` (OPTIONAL): The parameter used to detect logout requests.  By default, this is `logoutRequest` (matching the default for CAS).
* `security.shiro.cas.servicePath` (OPTIONAL): Required for a multi-domain configuration. This path is appended to the server's base-URL when constructing `security.shiro.cas.serviceUrl`. This should be the path relative to the server at which end-users can reach `/shiro-cas` within the current application.
* `security.shiro.cas.failurePath` (OPTIONAL): Optional with a multi-domain configuration. This path is appended to the server's base-URL when constructing `security.shiro.cas.failureUrl`, the URL that users are redirected to if ticket validation fails.

## Example configuration

Assuming that:
* your CAS instance is deployed at context path `/cas` on `sso.example.com` on the default HTTPS port
* your Grails application is accessed via a load-balancer at https://apps.example.com/my-app

A valid configuration would be:

```groovy
security.shiro.cas.serverUrl='https://sso.example.com/cas'
security.shiro.cas.serviceUrl='https://apps.example.com/my-app/shiro-cas'
```

### Example multi-domain configuration
Assuming that:
* your end-users use more than one domain to access the application
* your CAS instance is deployed at context path `/cas` on `sso.example.com` on the default HTTPS port
* your Grails application is accessed via a load-balancer at https://\*.example.com/my-app, where \* indicates a wildcard.

A valid configuration would be:

```groovy
security.shiro.cas.servicePath='/my-app/shiro-cas'
security.shiro.cas.serverUrl='https://sso.example.com/cas'
security.shiro.cas.serviceUrl='https://default.example.com' + security.shiro.cas.servicePath //default serviceUrl
```

# Accessing URLs

If, in your application, you need access to any of the configuration values, use `ShiroCasConfigUtils`.

For accessing the login/logout URLs, use `ShiroCasUrlBuilder`.  It supports adding query parameters if needed.

Example:

    ShiroCasUrlBuilder.forLogin().withRenew().withQueryParam("token", "12345").go(response)

# What does the plugin do?

In general, it's intended to make it quick and easy to use CAS by layering on top of the configuration already done
by the shiro plugin.  This includes:

* If Single Sign Out is not disabled:
  * configures an instance of `SingleSignOutFilter` as `singleSignOutFilter` and registers it in `shiroFilter`'s `filterChainDefinitions`
  * registers `SingleSignOutHttpSessionListener` in `web.xml`
* configures an instance of `CasFilter` as `casFilter` and registers it in `shiroFilter`'s `filterChainDefinitions`
* configures an instance of `Cas20ServiceTicketValidator` as `casTicketValidator`
* configures an instance of `CasSubjectFactory` as `casSubjectFactory` and registers it with `shiroSecurityManager`
