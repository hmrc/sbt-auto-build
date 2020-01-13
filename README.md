# sbt-auto-build

[![Build Status](https://travis-ci.org/hmrc/sbt-auto-build.svg?branch=master)](https://travis-ci.org/hmrc/sbt-auto-build) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-auto-build/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-auto-build/_latestVersion)

This auto-plugin provides and applies common settings used across the HMRC platform. 

Usage
-----

## Sbt 1.x

Since major version 2, this plugin is cross compiled for sbt 1.x (specifically 1.3.4).

> Also see upgrade notes for version 2 below

| Sbt version | Plugin version |
| ----------- | -------------- |
| `0.13.x`    | `any`          |
| `>= 1.x`    | `>= 2.x`       |

In your project/plugins.sbt file:
```
resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "x.x.x")
```

where 'x.x.x' is the latest release as advertised above.

We have added the resolver here, if you already have the 'https://dl.bintray.com/hmrc/sbt-plugin-releases' repo added there's no need to re-add it here.

Add the line ```.enablePlugins(SbtAutoBuildPlugin)``` to your project to enable the plugin.

## Upgrading to the latest release

### Required header/licence setup

In order to apply the correct licence or copyright headers to the start of all of your source files, 
`sbt-auto-build` now enforces that:

 1. Your repository contains a `repository.yaml` file at the root of the project, with a valid `repoVisibility` identifier.
 See [here](https://confluence.tools.tax.service.gov.uk/x/k_8TCQ) for more info
 1. If your repository is marked as being *public*: A `LICENSE` file _must_ exist, and must be the Apache V2.0 licence, like [this one](https://github.com/hmrc/service-dependencies/blob/master/LICENSE)
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

It also automatically adds the  *sbt-utils* and *sbt-header* plugins. SBT header is on by default and will generate licence headers in your source files. As a result **you don't have to add licence headers to source files manually**

To make the automatic addition of settings more visible you'll see output like this in your build:
```
[info] SbtAutoBuildPlugin adding 19 build settings:
[info] buildinfo, buildinfo, buildinfoBuildnumber, buildinfoKeys, buildinfoKeys, buildinfoObject, buildinfoPackage, buildinfoPackage, fork, headers, initialCommands, isSnapshot, organization, packageOptions, parallelExecution, scalaVersion, scalacOptions, sourceGenerators, testOptions
```
