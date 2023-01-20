/*
 * Copyright 2023 HM Revenue & Customs
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

import de.heikoseeberger.sbtheader.{AutomateHeaderPlugin, CommentStyle, HeaderPlugin, FileType}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{HeaderLicense, headerLicense, headerMappings, headerSources}
import sbt.Keys._
import sbt._

object SbtAutoBuildPlugin extends AutoPlugin {

  val logger = ConsoleLogger()

  val forceLicenceHeader = SettingKey[Boolean]("forceLicenceHeader", "forces generation of Apache V2 Licence headers")

  val currentYear: String = LocalDate.now().getYear.toString

  override def requires: Plugins = HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Setting[_]] = {

    // Taken from the sbt-twirl plugin to avoid declaring a full dependency (which this plugin used previously)
    // That caused potential evictions of the `twirl-api` library and inconsistencies depending on the version used by clients
    // See comment on https://jira.tools.tax.service.gov.uk/browse/BDOG-516
    val twirlCompileTemplates =
      TaskKey[Seq[File]]("twirl-compile-templates", "Compile twirl templates into scala source files")

    logger.info(s"SbtAutoBuildPlugin - adding build settings")

    Seq(
      Compile / headerSources ++= (Compile / twirlCompileTemplates / sources).value
    ) ++
      DefaultBuildSettings.scalaSettings ++
      SbtBuildInfo() ++
      DefaultBuildSettings.defaultSettings() ++
      PublishSettings() ++
      Seq(resolvers := HmrcResolvers.resolvers()) ++
      ArtefactDescription() ++
      Seq(forceLicenceHeader := false) ++
      HeaderSettings(forceLicenceHeader)
  }
}

object HmrcResolvers {
  private val logger = ConsoleLogger()

  private val artifactoryUrl = "https://artefacts.tax.service.gov.uk/artifactory"

  def resolvers(): Seq[Resolver] = Seq(
    Opts.resolver.sonatypeReleases,
    Resolver.typesafeRepo("releases")
  ) ++
    // try corporate artifactory before open artifacts, if reachable
    (if (isHealthy(s"$artifactoryUrl/api/system/ping"))
      Seq(
        MavenRepository("hmrc-releases", s"$artifactoryUrl/hmrc-releases/"),
        MavenRepository("third-party-maven-releases", s"$artifactoryUrl/third-party-maven-releases/"),
        Resolver.url("third-party-ivy-releases", url(s"$artifactoryUrl/third-party-ivy-releases"))(Resolver.ivyStylePatterns)
      )
     else Seq.empty
    ) ++
    Seq(MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"))

  private def isHealthy(healthcheck: String): Boolean = {
    val isReachable = {
      val conn = new java.net.URL(healthcheck).openConnection().asInstanceOf[java.net.HttpURLConnection]
      conn.setConnectTimeout(1000)
      try {
        conn.getResponseCode
        // we expect 200, but any response is enough to know that the hostname is reachable
        true
      } catch {
        case _: java.net.SocketTimeoutException => false
        case _: Throwable => true // an optimisation may be to mark as not reachable if not responding well
      }
    }
    if (!isReachable) {
      logger.warn(s"$healthcheck is not reachable")
    }
    isReachable
  }
}

object PublishSettings {
  def apply(): Seq[Def.Setting[Boolean]] = Seq(
    publishArtifact := true,
    Test / publishArtifact := false,
    IntegrationTest / publishArtifact  := false,
  )
}

// Enforce a standard licence header across all HMRC
// public repo -> apache v2
// private repo -> only copyright headers
object HeaderSettings {

  val commentStyles: Map[FileType, CommentStyle] = Map(
    FileType.scala   -> CommentStyle.cStyleBlockComment,
    FileType.conf    -> CommentStyle.hashLineComment,
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
    ) ++
    AutomateHeaderPlugin.autoImport.automateHeaderSettings(Compile, Test)
}
