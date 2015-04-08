/*
 * Copyright 2015 HM Revenue & Customs
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

import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import sbt._
import sbt.Keys._

object SbtAutoBuildPlugin extends AutoPlugin {

  import uk.gov.hmrc.DefaultBuildSettings._

  val logger = ConsoleLogger()

  private val addedSettings: Seq[Setting[_]] =
    scalaSettings ++ SbtBuildInfo() ++ defaultSettings() ++ HeaderSettings()

  override def requires = AutomateHeaderPlugin
  override def trigger = noTrigger

  override lazy val projectSettings = {

    // FIXME logging is output here because I can't find a place to hook
    // into for when the plugin is loaded and used
    logger.info(s"SbtAutoBuildPlugin adding ${addedSettings.size} build settings:")
    logger.info(addedSettings.map { s => s.key.key.label }.sorted.mkString(", "))

    Seq(
      targetJvm := "jvm-1.8",
      publishArtifact := true,
      publishArtifact in Test := false,
      publishArtifact in IntegrationTest := false,
      resolvers := Seq(
        Opts.resolver.sonatypeReleases,
        Resolver.bintrayRepo("hmrc", "releases")
      )
    )} ++ addedSettings ++ AutomateHeaderPlugin.projectSettings
}

object HeaderSettings {
  import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
  import de.heikoseeberger.sbtheader.license.Apache2_0
  import org.joda.time.DateTime

  def apply() = headers := Map("scala" -> Apache2_0(DateTime.now().getYear.toString, "HM Revenue & Customs"))
}
