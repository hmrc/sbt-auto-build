import java.time.LocalDate

import de.heikoseeberger.sbtheader.{CommentCreator, CommentStyle}
import uk.gov.hmrc.{SbtBuildInfo, DefaultBuildSettings}

lazy val project = Project("sbt-auto-build", file("."))
  .enablePlugins(SbtPlugin, AutomateHeaderPlugin)
  .settings(
    DefaultBuildSettings.scalaSettings ++
      SbtBuildInfo() ++
      DefaultBuildSettings.defaultSettings() ++
      headerSettings
  )
  .settings(
    sbtPlugin        := true,
    majorVersion     := 3,
    isPublicArtefact := true,
    scalaVersion     := "2.12.18",
    addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "5.10.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-setting-keys"   % "1.0.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-settings"       % "4.26.0"),
    addSbtPlugin("uk.gov.hmrc"       % "sbt-git-versioning" % "2.7.0"),
    libraryDependencies ++= Seq(
      "org.yaml"              %  "snakeyaml"            % "2.3",
      "org.eclipse.jgit"      %  "org.eclipse.jgit"     % "6.10.0.202406032230-r", // 7.x requires all clients to be on Java 21
      "org.scalatest"         %% "scalatest"            % "3.2.17" % Test,
      "com.vladsch.flexmark"  %  "flexmark-all"         % "0.64.8" % Test
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
  headerLicense  := Some(HeaderLicense.ALv2(LocalDate.now().getYear.toString, "HM Revenue & Customs")),
  headerMappings := headerMappings.value.mapValues(retainYearCommentCreator)
)

def retainYearCommentCreator(commentStyle: CommentStyle) =
  commentStyle.copy(commentCreator = new CommentCreator() {
    private val datePattern = "(?s).*?(\\d{4}).*".r

    private def findYear(header: String): Option[String] =
      header match {
        case datePattern(year) => Some(year)
        case _                 => None
      }

    override def apply(text: String, existingText: Option[String]): String = {
      val newText = commentStyle.commentCreator.apply(text, existingText)
      existingText
        .flatMap(findYear)
        .map(year => newText.replace(LocalDate.now().getYear.toString, year))
        .getOrElse(newText)
    }
  })
