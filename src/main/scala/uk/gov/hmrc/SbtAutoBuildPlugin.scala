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
import uk.gov.hmrc.HeaderUtils.{Private, Public}

object SbtAutoBuildPlugin extends AutoPlugin {

  import uk.gov.hmrc.DefaultBuildSettings._

  val logger = ConsoleLogger()

  val autoSourceHeader = SettingKey[Boolean]("autoSourceHeader", "generate open-source headers if LICENSE file exists")
  val forceSourceHeader = SettingKey[Boolean]("forceSourceHeader", "forces generation of open-source headers regardless of LICENSE")

  val currentYear = LocalDate.now().getYear.toString

  private val defaultAutoSettings: Seq[Setting[_]] =
    scalaSettings ++
      SbtBuildInfo() ++
      defaultSettings() ++
      PublishSettings() ++
      Resolvers() ++
      ArtefactDescription() ++
      Seq(autoSourceHeader := true, forceSourceHeader := false)

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
    ) ++ defaultAutoSettings ++ HeaderSettings(autoSourceHeader, forceSourceHeader)

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

  val license = new File("LICENSE")
  val repositoryYamlFile = new File("repository.yaml")

  val commentStyles: Map[FileType, CommentStyle] = Map(
    FileType.scala -> CommentStyle.cStyleBlockComment,
    FileType.conf -> CommentStyle.hashLineComment,
    FileType("html") -> CommentStyle.twirlStyleBlockComment
  )

  def shouldGenerateHeaders(autoSource: Boolean, force: Boolean): Boolean = {

    lazy val stringOrVisibility = for {
      yamlString <- HeaderUtils.loadRepositoryYamlFile(new File(""))
      yaml <- HeaderUtils.loadYaml(yamlString)
      repoVisibility <- HeaderUtils.getRepoVisiblity(yaml)
    } yield repoVisibility

    if (force) {
      SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - forceSourceHeader setting was set to true, Apache 2.0 licence file headers will be generated regardless")
      true
    } else stringOrVisibility match {
      case Right(Public) =>
        SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - repository is marked public. Licence headers will be added to all source files")
        true
      case Right(Private) =>
        SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - repository is marked private. No licence headers will be added to source files, only a copyright notice")
        false
      case Left(error) =>
        sys.error(s"SbtAutoBuildPlugin - Error, please fix: $error")
    }


    //    if (autoSource && license.exists()) {
    //      SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - LICENSE file exists, sbt-header will add Apache 2.0 license headers to each source file.")
    //      true
    //    } else {
    //      SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - No LICENSE file found, please add one to the root of your repo or set forceSourceHeader=true")
    //      false
  }

  def apply(autoSourceHeader: SettingKey[Boolean], forceSourceHeader: SettingKey[Boolean]): Seq[Setting[_]] = {
    Seq(
      headerLicense := {
        if (shouldGenerateHeaders(autoSourceHeader.value, forceSourceHeader.value))
          Some(HeaderLicense.ALv2(SbtAutoBuildPlugin.currentYear, "HM Revenue & Customs"))
        else Some(HeaderLicense.Custom(
          s"""|Copyright ${SbtAutoBuildPlugin.currentYear} HM Revenue & Customs
             |
             |""".stripMargin
        ))
      },
      headerMappings := headerMappings.value ++ commentStyles
//      Compile / headerCreate := Def.taskDyn {
//        val original = (Compile / headerCreate).taskValue
//        if(shouldGenerateHeaders(autoSourceHeader.value, forceSourceHeader.value)) {
//          SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - Will set licence headers")
//          Def.task(original.value)
//        }
//        else {
//          SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - no need to set licence headers")
//          Def.task(scala.collection.immutable.Iterable.empty[File])
//        }
//      }.value
    )
  }
}