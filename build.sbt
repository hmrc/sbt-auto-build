import java.time.LocalDate

import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.SbtBuildInfo

val pluginName = "sbt-auto-build"

lazy val project = Project(pluginName, file("."))
  .enablePlugins(AutomateHeaderPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    scalaSettings ++
      SbtBuildInfo() ++
      defaultSettings() ++
      headerSettings ++
      publishSettings ++
      artefactDescription: _*
  )
  .settings(
    sbtPlugin := true,
    majorVersion := 2,
    makePublicallyAvailableOnBintray := true,
    scalaVersion := "2.12.10",
    crossSbtVersions := Vector("0.13.18", "1.3.4"),
    targetJvm := "jvm-1.8",
    addSbtPlugin("de.heikoseeberger" % "sbt-header" % "4.1.0"),
    addSbtPlugin("uk.gov.hmrc" % "sbt-settings" % "4.1.0"),
    libraryDependencies ++= Seq(
      "org.yaml"              % "snakeyaml"             % "1.25",
      "org.eclipse.jgit"      % "org.eclipse.jgit.pgm"  % "4.11.9.201909030838-r" exclude("javax.jms", "jms") exclude("com.sun.jdmk", "jmxtools") exclude("com.sun.jmx", "jmxri"),
      "org.scalatest"         %% "scalatest"            % "3.1.0"     % Test,
      "com.vladsch.flexmark"  % "flexmark-all"          % "0.35.10"   % Test
    ),
    resolvers := Seq(
      Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
    ),
    useCoursier := false //Required to fix resolution for IntelliJ
  )

val publishSettings = Seq(
    publishArtifact := true,
    publishArtifact in Test := false,
    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false
  )

val headerSettings = {
  Seq(
    headerLicense := Some(HeaderLicense.ALv2(LocalDate.now().getYear.toString, "HM Revenue & Customs"))
  )
}

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
