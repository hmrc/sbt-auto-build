resolvers ++=
  Seq(Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns))

addSbtPlugin("uk.gov.hmrc" % "sbt-settings" % "3.5.0")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.8.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "0.9.0")
