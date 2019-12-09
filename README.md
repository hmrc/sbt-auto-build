# sbt-auto-build

[![Build Status](https://travis-ci.org/hmrc/sbt-auto-build.svg?branch=master)](https://travis-ci.org/hmrc/sbt-auto-build) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-auto-build/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-auto-build/_latestVersion)

This auto-plugin provides and applies common settings used across the HMRC platform. 

Usage
-----

### Sbt 1.x

Since major version 2, this plugin is cross compiled for sbt 1.x (specifically 1.3.4).

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