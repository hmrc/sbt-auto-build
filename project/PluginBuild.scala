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
import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._

object PluginBuild extends Build {

  val pluginName = "sbt-auto-build"
  val appVersion = "0.1.5-SNAPSHOT"

  lazy val root = (project in file("."))
    .settings(
      sbtPlugin := true,
      targetJvm := "jvm-1.7",
      organization := "uk.gov.hmrc",
      version := appVersion,
      name := pluginName,
      scalaVersion := "2.10.4",
      ArtefactDescription(),
      addSbtPlugin("uk.gov.hmrc" % "sbt-utils" % "2.6.0"),
      addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.4.1")
    )
}


object ArtefactDescription {

  def apply() =
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
}
