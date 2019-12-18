/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc

import java.time.LocalDate

import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader.{AutomateHeaderPlugin, CommentStyle, FileType}
import sbt.Keys._
import sbt.{Setting, _}
import uk.gov.hmrc.RepositoryYamlUtils.{Private, Public, RepoVisibility}

import scala.io.Source
import scala.util.Try

object SbtAutoBuildPlugin extends AutoPlugin {

  import uk.gov.hmrc.DefaultBuildSettings._

  val logger = ConsoleLogger()

  val forceLicenceHeader = SettingKey[Boolean]("forceLicenceHeader", "forces generation of Apache V2 Licence headers")

  val currentYear = LocalDate.now().getYear.toString

  private val defaultAutoSettings: Seq[Setting[_]] =
    scalaSettings ++
      SbtBuildInfo() ++
      defaultSettings() ++
      PublishSettings() ++
      Resolvers() ++
      ArtefactDescription() ++
      Seq(forceLicenceHeader := false)

  override def requires: Plugins = AutomateHeaderPlugin

  override def trigger: PluginTrigger = noTrigger

  override lazy val projectSettings: Seq[Setting[_]] = {

    // Taken from the sbt-twirl plugin to avoid declaring a full dependency (which this plugin used previously)
    // That caused potential evictions of the `twirl-api` library and inconsistencies depending on the version used by clients
    // See comment on https://jira.tools.tax.service.gov.uk/browse/BDOG-516
    val twirlCompileTemplates =
    TaskKey[Seq[File]]("twirl-compile-templates", "Compile twirl templates into scala source files")

    val addedSettings = Seq(
      // targetJvm declared here means that anyone using the plugin will inherit this by default. It only needs to
      // be specified by clients if they want to override it
      targetJvm := "jvm-1.8",
      unmanagedSources.in(Compile, headerCreate) ++= sources.in(Compile, twirlCompileTemplates).value
    ) ++ defaultAutoSettings ++ HeaderSettings(forceLicenceHeader)

    logger.info(s"SbtAutoBuildPlugin - adding ${addedSettings.size} build settings")

    addedSettings
  }
}

object Resolvers {

  val HmrcReleasesRepo = Resolver.bintrayRepo("hmrc", "releases")

  def apply(): Def.Setting[Seq[Resolver]] =
    resolvers := Seq(
      Opts.resolver.sonatypeReleases,
      Resolver.typesafeRepo("releases"),
      HmrcReleasesRepo
    )
}

object PublishSettings {
  def apply(): Seq[Def.Setting[Boolean]] = Seq(
    publishArtifact := true,
    publishArtifact in Test := false,
    publishArtifact in IntegrationTest := false,
    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false,
    publishArtifact in(IntegrationTest, packageDoc) := false,
    publishArtifact in(IntegrationTest, packageSrc) := false
  )
}

// Enforce a standard licence header across all HMRC
object HeaderSettings {
  import Extensions.RichTry

  val license = new File("LICENSE")
  val repositoryYamlFile = new File("repository.yaml")

  val expectedLicenceText =
    """Apache License
      |                           Version 2.0, January 2004""".stripMargin

  val commentStyles: Map[FileType, CommentStyle] = Map(
    FileType.scala -> CommentStyle.cStyleBlockComment,
    FileType.conf -> CommentStyle.hashLineComment,
    FileType("html") -> CommentStyle.twirlStyleBlockComment
  )

  def shouldGenerateHeaders(force: Boolean): Boolean = {

    // .right projection required to remain backwards compatible with scala 2.10 cross build (for sbt 0.13)
    lazy val errorOrVisibility = for {
      yamlString <- RepositoryYamlUtils.loadRepositoryYamlFile(new File("")).right
      yaml <- RepositoryYamlUtils.loadYaml(yamlString).right
      repoVisibility <- RepositoryYamlUtils.getRepoVisiblity(yaml).right
      _ <- checkLicenceFile(repoVisibility).right
    } yield repoVisibility

    def checkLicenceFile(repoVisibility: RepoVisibility): Either[String, Unit] = repoVisibility match {
      case Private => Right(())
      case Public =>
        if (license.exists()) {
          Try {
            val f = Source.fromFile(license)
            val content = f.mkString
            f.close()
            content
          }.toEither.left.map(_ => s"Problem reading licence file")
              // .right projection required to remain backwards compatible with scala 2.10 cross build (for sbt 0.13)
              .right.flatMap(c =>
                  if (c.contains(expectedLicenceText)) Right(())
                  else Left(s"The LICENSE file does not contain the appropriate Apache V2 licence")
          )
        }
        else Left(s"No LICENSE file exists but the repository is marked as public")
    }

    if (force) {
      SbtAutoBuildPlugin.logger.info(s"SbtAutoBuildPlugin - ${SbtAutoBuildPlugin.forceLicenceHeader.key.label} setting was set to true, Apache 2.0 licence file headers will be generated regardless")
      true
    } else errorOrVisibility match {
      case Right(Public) =>
        SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - repository is marked public. Licence headers will be added to all source files")
        true
      case Right(Private) =>
        SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - repository is marked private. No licence headers will be added to source files, only a copyright notice")
        false
      case Left(error) =>
        sys.error(s"SbtAutoBuildPlugin - Error, please fix: $error")
    }
  }

  def apply(forceSourceHeader: SettingKey[Boolean]): Seq[Setting[_]] = {
    Seq(
      headerLicense := {
        if (shouldGenerateHeaders(forceSourceHeader.value))
          Some(HeaderLicense.ALv2(SbtAutoBuildPlugin.currentYear, "HM Revenue & Customs"))
        else Some(HeaderLicense.Custom(
          s"""|Copyright ${SbtAutoBuildPlugin.currentYear} HM Revenue & Customs
              |
              |""".stripMargin
        ))
      },
      headerMappings := headerMappings.value ++ commentStyles
    )
  }
}