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
import org.eclipse.jgit.lib.StoredConfig
import sbt.Keys._
import sbt._

object SbtAutoBuildPlugin extends AutoPlugin {

  import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
  import uk.gov.hmrc.DefaultBuildSettings._

  val logger = ConsoleLogger()

  val autoSourceHeader = SettingKey[Boolean]("autoSourceHeader", "generate open-source header licences")

  private val defaultAutoSettings: Seq[Setting[_]] =
    scalaSettings ++
      SbtBuildInfo() ++
      defaultSettings() ++
      PublishSettings() ++
      Resolvers() ++
      ArtefactDescription() ++
      Seq(autoSourceHeader := true)

  override def requires = AutomateHeaderPlugin

  override def trigger = noTrigger

  override lazy val projectSettings = {

    val addedSettings = Seq(
      targetJvm := "jvm-1.8", //FIXME if this doesn't go here projects need to declare it
      headers := {
        if (autoSourceHeader.value) HeaderSettings() else Map.empty
      }
    ) ++ defaultAutoSettings

    logger.info(s"SbtAutoBuildPlugin adding ${addedSettings.size} build settings (duplicates represent different scopes):")
    logger.info(addedSettings.map { s => s.key.scopedKey.key.label }.sorted.mkString(", "))

    addedSettings
  }
}

object Resolvers {

  val HmrcReleasesRepo = Resolver.bintrayRepo("hmrc", "releases")

  def apply() =
    resolvers := Seq(
      Opts.resolver.sonatypeReleases,
      Resolver.typesafeRepo("releases"),
      HmrcReleasesRepo
    )
}

object PublishSettings {
  def apply() = Seq(
    publishArtifact := true,
    publishArtifact in Test := false,
    publishArtifact in IntegrationTest := false,
    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false,
    publishArtifact in(IntegrationTest, packageDoc) := false,
    publishArtifact in(IntegrationTest, packageSrc) := false
  )
}

object HeaderSettings {

  import de.heikoseeberger.sbtheader.license.Apache2_0
  import org.joda.time.DateTime

  val copyrightYear = DateTime.now().getYear.toString
  val copyrightOwner = "HM Revenue & Customs"

  def apply() = {
    Map(
      "scala" -> Apache2_0(copyrightYear, copyrightOwner),
      "conf" -> Apache2_0(copyrightYear, copyrightOwner, "#")
    )
  }
}

object ArtefactDescription {

  private val logger = ConsoleLogger()

  def apply() = Seq(
    homepage := Some(url(Git.browserUrl())),
    organizationHomepage := Some(url("https://www.gov.uk/government/organisations/hm-revenue-customs")),
    scmInfo := Some(ScmInfo(url(Git.browserUrl()), Git.remoteConnectionUrl)),

    // workaround for sbt/sbt#1834
    pomPostProcess := {

      import scala.xml.transform.{RewriteRule, RuleTransformer}
      import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

      (node: XmlNode) =>
        new RuleTransformer(new RewriteRule {
          override def transform(node: XmlNode): XmlNodeSeq = node match {
            case e: Elem if e.label == "developers" =>
              <developers>
                {developers.value.map { dev =>
                <developer>
                  <id>{dev.id}</id>
                  <name>{dev.name}</name>
                  <email>{dev.email}</email>
                  <url>{dev.url}</url>
                </developer>
              }}
              </developers>
            case _ => node
          }
        }).transform(node).head
    }
  )
}

object Git {

  val logger = ConsoleLogger()

  import scala.collection.JavaConversions._

  private val colonHmrc = ":hmrc".r
  private val forwardSlashHmrc = "\\/hmrc".r

  def browserUrl(connectionUrl: String = remoteConnectionUrl) = {
    val removedProtocol = removeProtocol(connectionUrl)
    s"https://${colonHmrc.replaceFirstIn(removedProtocol, "/hmrc")}"
  }

  lazy val remoteConnectionUrl = {
    val originUrl = gitConfig.getSubsections("remote")
      .map(remoteUrl)
      .headOption.getOrElse(throw new IllegalArgumentException("No git remote connection URL could be found"))

    val gitTcpRex = "^(git:\\/\\/)".r
    val gitAt: String = gitTcpRex.replaceFirstIn(originUrl, "git@")
    forwardSlashHmrc.replaceFirstIn(gitAt, ":hmrc")
  }

  private def remoteUrl(remoteName: String): String = {
    val remoteUrl = gitConfig.getString("remote", remoteName, "url")
    logger.info(s"The config section 'remote' with subsection '$remoteName' had a url of '$remoteUrl'")
    remoteUrl
  }

  private def removeProtocol(connectionUrl: String): String = {
    "^(git@|git:\\/\\/|.git)".r.replaceFirstIn(connectionUrl, "")
  }

  private lazy val gitConfig: StoredConfig = {
    import org.eclipse.jgit.storage.file.FileRepositoryBuilder
    val builder = new FileRepositoryBuilder
    val repository = builder.readEnvironment.findGitDir.build
    repository.getConfig
  }
}
