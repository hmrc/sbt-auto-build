import sbt.Resolver

resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "4.1.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-artifactory"    % "1.0.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-git-versioning" % "2.1.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"       % "4.7.0")
