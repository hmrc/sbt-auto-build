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
    scalaVersion := "2.12.18",
    addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "5.10.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-setting-keys"   % "0.4.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"       % "4.17.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-git-versioning" % "2.4.0"),
    libraryDependencies ++= Seq(
      "org.yaml"              %  "snakeyaml"            % "1.25",
      "org.eclipse.jgit"      %  "org.eclipse.jgit"     % "4.11.9.201909030838-r",
      "commons-codec"         %  "commons-codec"        % "1.15", // updates version provided by org.eclipse.jgit
      "org.scalatest"         %% "scalatest"            % "3.1.0"     % Test,
      "com.vladsch.flexmark"  %  "flexmark-all"         % "0.35.10"   % Test
    ),
    resolvers := Seq(
      MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),
      Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
    ),
    scriptedRun := {

      // sbt >= 1.4, the plugin under test is loaded before running `test` script.
      // We need to gitify the test projects before loading the plugin.
      // Hooking in here for now - https://github.com/sbt/sbt/issues/2601
      import scala.sys.process.Process
      val scriptedRepoDir = sourceDirectory.value / "sbt-test" / "sbt-auto-build"
      Process(List("ls"), scriptedRepoDir).lineStream.foreach { f =>
        val exitCode = Process("git init", scriptedRepoDir / f).#&&(
          Process(List("git", "commit", "--allow-empty", "-m \"Initial commit to allow SbtGitVersioning to function\""), scriptedRepoDir / f)).!
        if (exitCode != 0)
          sys.error(s"Failed to initialise test repos with git - exitCode: $exitCode")
      }

      scriptedRun.value
    },
    scriptedLaunchOpts ++= {
      val homeDir = sys.props.get("jenkins.home").orElse(sys.props.get("user.home")).getOrElse("")
      val sbtHome = file(homeDir) / ".sbt"
      Seq(
        "-Xmx1024M",
        "-Dplugin.version=" + version.value,
        s"-Dsbt.override.build.repos=${sys.props.getOrElse("sbt.override.build.repos", "false")}",
        // s"-Dsbt.global.base=$sbtHome/.sbt",
        // Global base is overwritten with <tmp scripted>/global and can not be reconfigured
        // We have to explicitly set all the params that rely on base
        s"-Dsbt.boot.directory=${sbtHome / "boot"}",
        s"-Dsbt.repository.config=${sbtHome / "repositories"}",
        s"-Dsbt.boot.properties=file:///${sbtHome / "sbt.boot.properties"}",
      )
    },
    scriptedBufferLog := false
  )

val headerSettings = Seq(
    headerLicense := Some(HeaderLicense.ALv2(LocalDate.now().getYear.toString, "HM Revenue & Customs"))
  )
