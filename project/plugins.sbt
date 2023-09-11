resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "5.7.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"       % "4.11.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-setting-keys"   % "0.3.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-git-versioning" % "2.4.0")
