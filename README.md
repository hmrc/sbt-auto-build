# sbt-auto-build

[![Build Status](https://travis-ci.org/hmrc/sbt-auto-build.svg?branch=master)](https://travis-ci.org/hmrc/sbt-auto-build) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-auto-build/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-auto-build/_latestVersion)

This auto-plugin provides and applies common settings used across the HMRC platform. 

Usage
-----

### Sbt 1.x

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

## Upgrading to version 2

### Configuring the LICENSE file

TL;DR: If you upgrade and get an error with your build of `(compile:headerCreate) Unable to auto detect project license`,
then you need to add the appropriate licence file.

This plugin will add the copyright headers to the start of each of your files automatically, and to do that it needs to 
know the correct licence to apply.

There are 3 ways that can be done, in order of preference (we recommend using option 1):

1. Add a `LICENSE` file in it's root, like [this one](https://github.com/hmrc/service-dependencies/blob/master/LICENSE)
> Note the spelling of LICENSE with an `S` not a `C`, for legacy reasons 
1. Set `forceSourceHeader=true` in the build. This will add the Apache v2 licence.
1. Configure the licence directly as per the [sbt-header readme](https://github.com/sbt/sbt-header)

#### Why is this required now?
As part of upgrading all our plugins to be cross-built for sbt 1.x, we've also taken the opportunity to update some of the
underlying libraries that get pulled in. One of these is the [sbt-header](https://github.com/sbt/sbt-header) plugin. 

The newer version of the plugin has some internal changes, which makes our default behaviour different in the event there
is no licence. In versions < 2.x we defaulted to not requiring a licence, and not trying to apply one. Every repository
should have correct licence headers though, so the new behaviour is purposeful. The majority of repos already have the
LICENSE file and should not have any issue.

What it does
------------

When enabled sbt-auto-build automatically adds the most commonly used settings in [sbt-utils](https://github.com/hmrc/) which are the settings collections:

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