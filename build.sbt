import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.SbtBuildInfo

val pluginName = "sbt-auto-build"

lazy val project = Project(pluginName, file("."))
  .enablePlugins(AutomateHeaderPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    scalaSettings ++
      SbtBuildInfo() ++
      defaultSettings() ++
      publishSettings ++
      artefactDescription: _*
  )
  .settings(
    sbtPlugin := true,
    majorVersion := 1,
    makePublicallyAvailableOnBintray := true,
    scalaVersion := "2.10.7",
    targetJvm := "jvm-1.7",
    headers := headerSettings,
    addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1"),
    addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.8.0"),
    addSbtPlugin("uk.gov.hmrc" % "sbt-settings" % "3.8.0"),
    addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.17.0"),
    addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.15.0"),
    libraryDependencies ++= Seq(
      "org.eclipse.jgit" % "org.eclipse.jgit.pgm" % "3.7.0.201502260915-r",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test",
      "org.pegdown" % "pegdown" % "1.5.0" % "test"
    ),
    resolvers := Seq(
      Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
    )
  )

val publishSettings = Seq(
    publishArtifact := true,
    publishArtifact in Test := false,
    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false
  )

val headerSettings = {
  import de.heikoseeberger.sbtheader.license.Apache2_0
  import org.joda.time.DateTime

  Map("scala" -> Apache2_0(DateTime.now().getYear.toString, "HM Revenue & Customs"))
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
