resolvers ++= Seq(
  Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
  )

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1")

addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.8.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.17.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.16.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-settings" % "3.9.0")
