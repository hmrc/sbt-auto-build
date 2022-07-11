import java.time.LocalDate

import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.SbtBuildInfo

lazy val project = Project("sbt-auto-build", file("."))
  .enablePlugins(SbtPlugin, AutomateHeaderPlugin)
  .settings(
    DefaultBuildSettings.scalaSettings ++
      SbtBuildInfo() ++
      DefaultBuildSettings.defaultSettings() ++
      headerSettings
  )
  .settings(
    sbtPlugin := true,
    majorVersion := 3,
    isPublicArtefact := true,
    scalaVersion := "2.12.14",
    crossSbtVersions := Vector("1.3.4"),
    addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "5.7.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-setting-keys"   % "0.3.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"       % "4.11.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-git-versioning" % "2.4.0"),
    libraryDependencies ++= Seq(
      "org.yaml"              %  "snakeyaml"            % "1.25",
      "org.eclipse.jgit"      %  "org.eclipse.jgit"     % "4.11.9.201909030838-r",
      "org.scalatest"         %% "scalatest"            % "3.1.0"     % Test,
      "com.vladsch.flexmark"  %  "flexmark-all"         % "0.35.10"   % Test
    ),
    resolvers := Seq(
      MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),
      Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
    ),
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false
  )

val headerSettings = Seq(
    headerLicense := Some(HeaderLicense.ALv2(LocalDate.now().getYear.toString, "HM Revenue & Customs"))
  )
