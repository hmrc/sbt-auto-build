resolvers ++= Seq(
  Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
)

resolvers += Resolver.bintrayRepo("hmrc", "releases")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "3.0.2")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "1.0.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.0.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-settings" % "4.0.0")
