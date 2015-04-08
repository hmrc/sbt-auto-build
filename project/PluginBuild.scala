/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt._
import Keys._

object PluginBuild extends Build {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import de.heikoseeberger.sbtheader.AutomateHeaderPlugin

  val pluginName = "sbt-auto-build"
  val appVersion = "0.1.2"

  lazy val root = Project(pluginName, base = file("."), settings =
    Seq(
    sbtPlugin := true,
    version := appVersion,
    organization := "uk.gov.hmrc",
    name := pluginName,
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      Opts.resolver.sonatypeReleases
    ),
    publishArtifact := true,
    publishArtifact in Test := false,
    addSbtPlugin("uk.gov.hmrc" % "sbt-utils" % "2.5.0"),
    addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.4.1"),
    HeaderSettings()
  ) ++ ArtefactDescription() ++ defaultSettings()
  ).enablePlugins(AutomateHeaderPlugin)
}


object ArtefactDescription {

  def apply() = Seq(
      pomExtra := (<url>https://www.gov.uk/government/organisations/hm-revenue-customs</url>
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
        </developers>)
    )

}

object HeaderSettings {
  import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
  import de.heikoseeberger.sbtheader.license.Apache2_0

  def apply() = headers := Map("scala" -> Apache2_0("2015", "HM Revenue & Customs"))
}
