# sbt-auto-build

[![Build Status](https://travis-ci.org/hmrc/sbt-auto-build.svg?branch=master)](https://travis-ci.org/hmrc/sbt-auto-build) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-auto-build/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-auto-build/_latestVersion)


Simpler build settings by using an auto-plugin to automatically add settings from sbt-utils.

Usage
-----

In your project/plugins.sbt file:
```
resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "x.x.x")
```

where 'x.x.x' is the latest release as advertised above.

We have added the resolver here, if you already have the 'https://dl.bintray.com/hmrc/sbt-plugin-releases' repo added there's no need to re-add it here.

What it does
------------

Currently sbt-auto-build automatically adds the most comonly used settings in [sbt-utils](https://github.com/hmrc/) which are currently in settings collections called scalaSettings, SbtBuildInfo, defaultSettings and HeaderSettings. It also automatically adds sbt-utils and sbt-header plugins. SBT header is on by default and will generate licence headers in your source files. As a result you don't have to conficure any of these

To make the automatic addition of settings more visible you'll see output like this in your build:
```
[info] SbtAutoBuildPlugin adding 19 build settings:
[info] buildinfo, buildinfo, buildinfoBuildnumber, buildinfoKeys, buildinfoKeys, buildinfoObject, buildinfoPackage, buildinfoPackage, fork, headers, initialCommands, isSnapshot, organization, packageOptions, parallelExecution, scalaVersion, scalacOptions, sourceGenerators, testOptions
```

Updating from sbt-utils
-----------------------

See and example of this on the [time](https://github.com/hmrc/time) project: [HmrcBuild.scala](https://github.com/hmrc/time/blob/83cc7a509f13bacd6d4180fde74ad601bd45dd41/project/HmrcBuild.scala#L12-23) and 
[plugins.sbt](https://github.com/hmrc/time/blob/83cc7a509f13bacd6d4180fde74ad601bd45dd41/project/plugins.sbt)

1. Remove references to sbt-utils and sbt-header form your project/plugins.sbt file and add the code listed in 'Usage' above
2. Remove references to scalaSettings, SbtBuildInfo, defaultSettings and HeaderSettings in your project/HmrcBuild.scala file
