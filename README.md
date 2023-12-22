# sbt-auto-build



This auto-plugin provides and applies common settings used across the HMRC platform.

Usage
-----

In your project/plugins.sbt file:
```
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "x.x.x")
```

where 'x.x.x' is the latest release.

## Sbt 1.x

The library has supported sbt 1.x since version `2.0.0` and has dropped support for sbt 0.13 since `3.7.0`

| Sbt version | Plugin version |
| ----------- | -------------- |
| `>= 1.x`    | `>= 2.0.0`     |
| `0.13.x`    | `<= 3.6.0`     |


## Upgrading to the latest release

### Version 3.0.0

Since 3.0.0, this plugin is auto-enabled. You can remove ```.enablePlugins(SbtAutoBuildPlugin)``` from your build.sbt.
You will need to disable the plugin explicitly if you do not want to activate it for a module.

If you use the `makePublicallyAvailableOnBintray` setting provided by `sbt-artifactory`, this setting has been replaced by `isPublicArtefact` which is made available through `sbt-auto-build`. Please use `isPublicArtefact` instead, and remove `sbt-artifactory` from your project's plugins.sbt. This version is incompatible with a pre-1.15.0 version of sbt-artifactory; remove the sbt-artifactory dependency, as it's not required now.

`sbt-git-versioning` is now provided transitively, and does not need to be added to your project's plugins.sbt. Note, `SbtGitVersioning` is also an auto-plugin and does not require explicit enabling.

### Required header/licence setup

In order to apply the correct licence or copyright headers to the start of all of your source files,
`sbt-auto-build` now enforces that:

 1. Your repository contains a `repository.yaml` file at the root of the project, with a valid `repoVisibility` identifier.
 See [here](https://confluence.tools.tax.service.gov.uk/x/k_8TCQ) for more info
 1. If your repository is marked as being *public*: A `LICENSE` file _must_ exist, and must be the Apache V2.0 licence, like [this one](https://github.com/hmrc/service-dependencies/blob/HEAD/LICENSE)
 1. If your repository is marked as being *private*: A `LICENSE` file _must not_ exist

> Note the spelling of the LICENSE file with an `S` not a `C`

### Overriding the licence settings

In the unlikely event that there is a need to tweak the licence headers added, then it is possible to override the settings
used by the underlying [sbt-header](https://github.com/sbt/sbt-header) plugin.

> There is also a settingKey `forceSourceHeader=true` which forces the generation of the Apache V2 licence regardless.
> This should _not_ be used without good reason

### Why is this required now?
As part of upgrading all our plugins to be cross-built for sbt 1.x, we've also taken the opportunity to update some of the
underlying libraries and revisit how the settings are applied.

In versions < 2.x we defaulted to not requiring a licence, and not trying to apply one. Every repository
should have correct licence headers though, so the new behaviour is purposeful. The majority of public repos already have the
LICENSE file and should not have any issue.

If your repository is private, then when upgrading to the latest `sbt-auto-build` all your source files will be updated
to remove the licence info and replace it with a standard copyright notice.

What it does
------------

When enabled sbt-auto-build automatically adds the most commonly used settings in [sbt-settings](https://github.com/hmrc/sbt-settings) which are the settings collections:

* scalaSettings
* SbtBuildInfo
* defaultSettings
* HeaderSettings

It also automatically adds the  *sbt-settings* and *sbt-header* plugins. SBT header is on by default and will generate licence headers in your source files. As a result **you don't have to add licence headers to source files manually**.

The year of copyright in the header is set on initial creation of the header and is not updated each year, earlier versions took the legally unnecessary step of changing the copyright date every year.

To enable licence headers for IntegrationTests, just ensure you are using `.settings(DefaultBuildSettings.integrationTestSettings())` rather than `.settings(inConfig(IntegrationTest)(Defaults.itSettings))`

## Changes
### Version 3.18.0
- `DefaultBuildSettings.itSettings` has changed to `DefaultBuildSettings.itSettings()` and now defaults to a single subprocess (JVM) for all tests which is quicker. If you still require the extra isolation of a distinct subprocess per test, then use `DefaultBuildSettings.itSettings(forkJvmPerTest = true)`.
### Version 3.17.0
- Targets JVM 11 by default.

  This will require Scala 2.12 >= 2.12.16 and 2.13 >= 2.13.1.
### Version 3.15.0
- Settings now support `ThisBuild` scope.
### Version 3.13.0
- Existing copyright years are no longer replaced with the current year, but preserved.
