# Unreleased

* Use configured `servicePath` instead of hardcoded default in filter configuration

# 0.5.0 (2015-02-25)

* Simplify configuration; now the only required configuration is `security.shiro.cas.serverUrl`
* Multi-domain support used by default
* Removed support for `security.shiro.cas.serviceUrl`
  * In many cases, you can simply remove your old `security.shiro.cas.serviceUrl` configuration
  * If this doesn't work for you, replace it with `security.shiro.cas.baseServiceUrl`, removing the `/shiro-cas` suffix
  * If you use a custom callback path, specify it using `security.shiro.cas.servicePath`
* Removed support for `security.shiro.cas.failureUrl`
  * Instead, configure `security.shiro.cas.failurePath` if desired
* New default value for `security.shiro.cas.failurePath`

# 0.4.0 (2015-01-27)

* Add multi-domain support

# 0.3.3 (2014-08-29)

* Set `casTicketValidator` to use `UTF-8` as the encoding by default

# 0.3.2

* Fix `filterChainDefinitions` configuration when Single Sign Out enabled [#6](https://github.com/commercehub-oss/grails-shiro-cas/issues/6)

# 0.3.1

* Correct logic for registering single sign out HTTP session listener in `web.xml`

# 0.3.0

* Update shiro plugin dependency to 1.2.1
* Add support for Single Sign Out [#5](https://github.com/commercehub-oss/grails-shiro-cas/issues/5)
* Add ShiroCasUrlBuilder to support building login URLs with additional parameters [#4](https://github.com/commercehub-oss/grails-shiro-cas/issues/4)

# 0.2.0

* Add support for configuring `loginParameters` [#2](https://github.com/commercehub-oss/grails-shiro-cas/issues/2), thanks to [Gavin Hogan](https://github.com/gavinhogan).

# 0.1.0

* Initial version, based on [contribution](https://github.com/pledbrook/grails-shiro/pull/10) by [Ford Guo](https://github.com/fordguo).
* The `security.shiro.cas.enable` configuration item is no longer used.
