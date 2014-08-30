# Unreleased

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
