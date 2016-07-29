/*
 * Copyright 2016 HM Revenue & Customs
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
import org.eclipse.jgit.lib.{BranchConfig, Repository}
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

    logger.info(s"SbtAutoBuildPlugin adding ${addedSettings.size} build settings")

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
    homepage := Git.homepage,
    organizationHomepage := Some(url("https://www.gov.uk/government/organisations/hm-revenue-customs")),
    scmInfo := buildScmInfo,

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

  def buildScmInfo:Option[ScmInfo]={
    for(connUrl <- Git.findRemoteConnectionUrl;
        browserUrl <- Git.browserUrl)
        yield ScmInfo(url(browserUrl), connUrl)
  }
}

object Git extends Git {
  override lazy val repository: Repository = {
    import org.eclipse.jgit.storage.file.FileRepositoryBuilder
    val builder = new FileRepositoryBuilder
    builder.findGitDir.build
  }
}

trait Git {
  val logger = ConsoleLogger()
  val repository: Repository

  def homepage: Option[URL] = browserUrl map url

  def browserUrl: Option[String] = {
    findRemoteConnectionUrl map browserUrl
  }

  def findRemoteConnectionUrl: Option[String] = {
    val currentBranchUrl = getUrlForBranch(repository.getBranch())

    val url = currentBranchUrl.orElse(getUrlForBranch("master"))

    url.map { originUrl =>
      val gitTcpRex = "^(git:\\/\\/)".r
      gitTcpRex.replaceFirstIn(originUrl, "git@")
    }
  }

  private def getUrlForBranch(name: String) = {
    val config = repository.getConfig
    val branchConfig = new BranchConfig(config, name)
    Option(config.getString("remote", branchConfig.getRemote, "url"))
  }

  private def browserUrl(remoteConnectionUrl: String): String = {
    val removedProtocol = removeProtocol(remoteConnectionUrl)
    s"https://${removedProtocol.toLowerCase.replaceFirst(":", "/")}"
  }

  private def remoteUrl(remoteName: String): Option[String] = {
    val remoteUrl = Option(repository.getConfig.getString("remote", remoteName, "url"))
    logger.info(s"The config section 'remote' with subsection '$remoteName' had a url of '$remoteUrl'")
    remoteUrl
  }

  private def removeProtocol(connectionUrl: String): String = {
    "^(git@|git:\\/\\/|.git)".r.replaceFirstIn(connectionUrl, "")
  }
}
