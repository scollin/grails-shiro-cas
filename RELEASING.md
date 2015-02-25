In general, this plugin is intended to follow [Semantic Versioning](http://semver.org/).  During the pre-release period (pre-1.0), minor versions will be incremented whenever new features are added (which may include breaking changes), and fix versions will be incremented for fix-only releases (no new features).

To release a new version of the plugin:

1. If needed, get permission to publish this plugin to the Grails plugin portal and configure your `~/.grails/settings.groovy` (see [Creating Plugins](https://grails.org/wiki/Creating%20Plugins))
1. Review `README.md` and `CHANGES.md` to make sure they're up-to-date.  In particular, all changes that will be included in this release should be in the change log's "Unreleased" section.
1. Check that the [Travis CI build](https://travis-ci.org/commercehub-oss/grails-shiro-cas) is passing.
1. Add a commit with message "version: *VERSION*", where *VERSION* is the version you're going to release.  In this commit, update the version in `ShiroCasGrailsPlugin.groovy` and add a section for the version in `CHANGES.md`.
1. Add a git tag with the name of the version (no prefix or suffix).
1. Run `./grailsw publish-plugin`
1. Add a commit with message "version: *VERSION*", where *VERSION* is the released version with the fix level incremented, and a suffix of "-SNAPSHOT".  In this commit, update the version in `ShiroCasGrailsPlugin.groovy`.
1. Push the git changes, including tags.
1. In the [GitHub release page](https://github.com/commercehub-oss/grails-shiro-cas/releases), click "Draft a new release".  Select the new tag, use the version number as the release title, and copy-paste the change log entries for the new version (as markdown) into the "Describe this release" section... and publish!
1. Check that the [plugin portal page](http://grails.org/plugin/shiro-cas) updated properly.
