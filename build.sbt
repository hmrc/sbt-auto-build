import java.time.LocalDate

import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.SbtBuildInfo

val pluginName = "sbt-auto-build"

lazy val project = Project(pluginName, file("."))
  .enablePlugins(SbtPlugin)
  .enablePlugins(AutomateHeaderPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    DefaultBuildSettings.scalaSettings ++
      SbtBuildInfo() ++
      DefaultBuildSettings.defaultSettings() ++
      headerSettings ++
      publishSettings ++
      artefactDescription: _*
  )
  .settings(
    sbtPlugin := true,
    majorVersion := 3,
    makePublicallyAvailableOnBintray := true,
    scalaVersion := "2.12.10",
    crossSbtVersions := Vector("0.13.18", "1.3.4"),
    DefaultBuildSettings.targetJvm := "jvm-1.8",
    addSbtPlugin("de.heikoseeberger" % "sbt-header"       % "5.0.0"), // last cross-compiled version
    addSbtPlugin("uk.gov.hmrc"       % "sbt-setting-keys" % "0.1.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"     % "4.8.0"),
    libraryDependencies ++= Seq(
      "org.yaml"              %  "snakeyaml"            % "1.25",
      "org.eclipse.jgit"      %  "org.eclipse.jgit"     % "4.11.9.201909030838-r",
      "org.scalatest"         %% "scalatest"            % "3.1.0"     % Test,
      "com.vladsch.flexmark"  %  "flexmark-all"         % "0.35.10"   % Test
    ),
    resolvers := Seq(
      Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
    ),
    useCoursier := false, //Required to fix resolution for IntelliJ
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    sbtVersion := (pluginCrossBuild / sbtVersion).value
  )

val publishSettings = Seq(
    publishArtifact := true,
    publishArtifact in Test := false,
    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false
  )

val headerSettings = Seq(
    headerLicense := Some(HeaderLicense.ALv2(LocalDate.now().getYear.toString, "HM Revenue & Customs"))
  )

val artefactDescription =
    pomExtra := <url>https://www.gov.uk/government/organisations/hm-revenue-customs</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git@github.com:hmrc/sbt-auto-build.git</connection>
        <developerConnection>scm:git@github.com:hmrc/sbt-auto-build.git</developerConnection>
        <url>git@github.com:hmrc/sbt-auto-build.git</url>
      </scm>
      <developers>
        <developer>
          <id>charleskubicek</id>
          <name>Charles Kubicek</name>
          <url>http://www.equalexperts.com</url>
        </developer>
        <developer>
          <id>duncancrawford</id>
          <name>Duncan Crawford</name>
          <url>http://www.equalexperts.com</url>
        </developer>
      </developers>
