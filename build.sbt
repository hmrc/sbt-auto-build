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
    addSbtPlugin("uk.gov.hmrc"       % "sbt-setting-keys"   % "0.3.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"       % "4.13.0"),
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
