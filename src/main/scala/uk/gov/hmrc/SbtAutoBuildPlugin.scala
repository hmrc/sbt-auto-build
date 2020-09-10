/*
 * Copyright 2020 HM Revenue & Customs
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

object SbtAutoBuildPlugin extends AutoPlugin {

  val logger = ConsoleLogger()

  val forceLicenceHeader = SettingKey[Boolean]("forceLicenceHeader", "forces generation of Apache V2 Licence headers")

  val currentYear = LocalDate.now().getYear.toString

  private val defaultAutoSettings: Seq[Setting[_]] =
    DefaultBuildSettings.scalaSettings ++
      SbtBuildInfo() ++
      DefaultBuildSettings.defaultSettings() ++
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
      DefaultBuildSettings.targetJvm := "jvm-1.8",
      unmanagedSources.in(Compile, headerCreate) ++= sources.in(Compile, twirlCompileTemplates).value
    ) ++ defaultAutoSettings ++ HeaderSettings(forceLicenceHeader)

    logger.info(s"SbtAutoBuildPlugin - adding ${addedSettings.size} build settings")

    addedSettings
  }
}

object Resolvers {
  def apply(): Def.Setting[Seq[Resolver]] =
    resolvers := {
      // reduce timeout in case artifactory is not reachable (default is 2 min timeout)
      System.setProperty("sun.net.client.defaultConnectTimeout", "1000")
      Seq(
        Opts.resolver.sonatypeReleases,
        Resolver.typesafeRepo("releases"),
        // try internal artifactory before bintray
        Resolver.url("hmrc-releases", url("https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"))(Resolver.ivyStylePatterns),
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.bintrayIvyRepo("hmrc", "sbt-plugin-releases") // pointless? need to access the repo to get the sbt-auto-build plugin // is there a artifactory location to try first?
      )
    }
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
// public repo -> apache v2
// private repo -> only copyright headers
object HeaderSettings {

  val commentStyles: Map[FileType, CommentStyle] = Map(
    FileType.scala -> CommentStyle.cStyleBlockComment,
    FileType.conf -> CommentStyle.hashLineComment,
    FileType("html") -> CommentStyle.twirlStyleBlockComment
  )

  def apply(forceSourceHeader: SettingKey[Boolean]): Seq[Setting[_]] =
    Seq(
      headerLicense := {
        if (HeaderUtils.shouldGenerateHeaders(forceSourceHeader.value))
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
