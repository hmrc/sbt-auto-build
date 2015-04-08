# sbt-auto-build

[![Build Status](https://travis-ci.org/sbt-auto-build/sbt-auto-build.svg?branch=master)](https://travis-ci.org/hmrc/sbt-auto-build) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-auto-build/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-auto-build/_latestVersion)


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

We have added the resolver here, if you already have the 'hmrc/sbt-plugin-releases' repo added there's no need to re-add it here.
