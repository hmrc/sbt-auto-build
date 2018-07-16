/*
 * Copyright 2018 HM Revenue & Customs
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
import org.eclipse.jgit.lib.{BranchConfig, Repository, StoredConfig}
import play.twirl.sbt.Import.TwirlKeys
import sbt.Keys._
import sbt._

import scala.util.matching.Regex

object SbtAutoBuildPlugin extends AutoPlugin {

  import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
  import uk.gov.hmrc.DefaultBuildSettings._

  val logger = ConsoleLogger()

  val autoSourceHeader = SettingKey[Boolean]("autoSourceHeader", "generate open-source headers if LICENSE file exists")
  val forceSourceHeader = SettingKey[Boolean]("forceSourceHeader", "forces generation of open-source headers regardless of LICENSE")

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

    val license = new File("LICENSE")
    if (license.exists())
      logger.info("SbtAutoBuildPlugin - LICENSE file exists, sbt-header will add Apache 2.0 license headers to each source file.")
    else
      logger.info("SbtAutoBuildPlugin - No LICENSE found, source file Apache 2.0 header generation not required")

    val addedSettings = Seq(
      targetJvm := "jvm-1.8", //FIXME if this doesn't go here projects need to declare it
      headers := {
        if (forceSourceHeader.value)
          logger.info("SbtAutoBuildPlugin - forceSourceHeader setting was true, source file headers will be generated regardless of LICENSE")

        if ((license.exists() && autoSourceHeader.value) || forceSourceHeader.value) HeaderSettings() else Map.empty
      },
      unmanagedSources.in(Compile, createHeaders) ++= sources.in(Compile, TwirlKeys.compileTemplates).value
    ) ++ defaultAutoSettings

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

object HeaderSettings {

  import de.heikoseeberger.sbtheader.license.Apache2_0
  import org.joda.time.DateTime

  val copyrightYear: String = DateTime.now().getYear.toString
  val copyrightOwner: String = "HM Revenue & Customs"

  def apply(): Map[String, (Regex, String)] = {
    Map(
      "scala" -> Apache2_0(copyrightYear, copyrightOwner),
      "conf" -> Apache2_0(copyrightYear, copyrightOwner, "#"),
      "html" -> Apache2_0(copyrightYear, copyrightOwner, "@*")
    )
  }
}

object ArtefactDescription {

  def apply() = Seq(
    homepage := Git.homepage,
    organizationHomepage := Some(url("https://www.gov.uk/government/organisations/hm-revenue-customs")),
    scmInfo := buildScmInfo,

    // workaround for sbt/sbt#1834
    pomPostProcess := {

      import scala.xml.transform.{RewriteRule, RuleTransformer}
      import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

      node: XmlNode =>
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

  def buildScmInfo: Option[ScmInfo] = {
    for (connUrl <- Git.findRemoteConnectionUrl;
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
  lazy val config: StoredConfig = repository.getConfig

  def homepage: Option[URL] = browserUrl map url

  def browserUrl: Option[String] = {
    findRemoteConnectionUrl map browserUrl
  }

  def findRemoteConnectionUrl: Option[String] = {
    val currentBranchUrl = getUrlForBranch(repository.getBranch)

    val url = currentBranchUrl.orElse(getUrlForBranch("master")).orElse(getUrlForRemote("origin"))

    url.map { originUrl =>
      val gitTcpRex = "^(git:\\/\\/)".r
      gitTcpRex.replaceFirstIn(originUrl, "git@")
    }
  }

  private def getUrlForBranch(name: String) = {
    val branchConfig = new BranchConfig(config, name)
    getUrlForRemote(branchConfig.getRemote)
  }

  private def getUrlForRemote(name: String) = {
    Option(config.getString("remote", name, "url"))
  }

  private def browserUrl(remoteConnectionUrl: String): String = {
    val removedProtocol = removeProtocol(remoteConnectionUrl)
    val replacedSeparator = removedProtocol.toLowerCase.replaceFirst(":", "/")
    val removedGitSuffix = replacedSeparator.replaceFirst(".git$", "")
    s"https://$removedGitSuffix"
  }

  private def removeProtocol(connectionUrl: String): String = {
    "^(git@|git://|https://)".r.replaceFirstIn(connectionUrl, "")
  }
}
