/*
 * Copyright 2021 HM Revenue & Customs
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

import org.eclipse.jgit.lib.{BranchConfig, Repository, StoredConfig}
import sbt.{ConsoleLogger, URL, _}

object GitUtils extends GitUtils {
  override lazy val repository: Repository = {
    import org.eclipse.jgit.storage.file.FileRepositoryBuilder
    val builder = new FileRepositoryBuilder
    builder.findGitDir.build
  }
}

trait GitUtils {
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

  private def getUrlForBranch(name: String): Option[String] = {
    val branchConfig = new BranchConfig(config, name)
    getUrlForRemote(branchConfig.getRemote)
  }

  private def getUrlForRemote(name: String): Option[String] = {
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
