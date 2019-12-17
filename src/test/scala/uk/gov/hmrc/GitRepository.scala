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

import java.nio.file.Path

import org.eclipse.jgit.revwalk.RevCommit
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.reflect.io.File

trait GitRepository extends BeforeAndAfterEach { this: Suite =>

  // The use of var looks like a concurrency issue, but ScalaTest will actually create a new instance of the Suite
  // per Test, and is the recommended practice. See fixtures section in http://doc.scalatest.org/2.2.6/index.html#org.scalatest.FlatSpec
  var tempWorkDir: Path = _
  var gitHelper: GitHelper = _

  // GitInfo, whilst could be declared in each test, is in the fixture, as it should be cleaned up after each test by calling close()
  var git: GitUtils = _

  override def beforeEach() = {
    tempWorkDir = java.nio.file.Files.createTempDirectory("git_test")
    gitHelper = new GitHelper(tempWorkDir)
    git = new GitUtils {
      override val repository = {
        gitHelper.repo
      }
    }
    super.beforeEach() // To be stackable, must call super.beforeEach
  }

  override def afterEach() = {
    try {
      super.afterEach() // To be stackable, must call super.afterEach
    }
    finally {
      gitHelper.close()
      assert(new File(tempWorkDir.toFile).deleteRecursively(), "failed to delete file")
    }
  }
}

import java.nio.file.Path

class GitHelper(tempWorkDir: Path) {

  val jgit = org.eclipse.jgit.api.Git.init().setDirectory(tempWorkDir.toFile).call()
  val repo = jgit.getRepository

  val currentBranch = repo.getFullBranch
  val gitConfig = repo.getConfig

  def setRemote(name: String, url: String) = {
    gitConfig.setString("remote", name, "url", url)
    gitConfig.setString("remote", name, "fetch", s"+refs/heads/*:refs/remotes/$name/*")
  }

  def setBranch(name: String, remote: String) = {
    gitConfig.setString("branch", name, "remote", remote)
    gitConfig.setString("branch", name, "merge", s"refs/heads/$name")
  }

  def setRemoteWithBranch(remote: String, branch: String, url: String) = {
    setRemote(remote, url)
    setBranch(branch, remote)
  }

  def checkoutNewBranch(name: String) = {
    val checkout = jgit.checkout()
    checkout.setName(name)
    checkout.setCreateBranch(true)
    checkout.call()
  }

  def checkoutBranch(commit: RevCommit) = {
    val checkout = jgit.checkout()
    checkout.setStartPoint(commit)
    checkout.setCreateBranch(true)
    checkout.setName("test")
    checkout.call()
  }

  def createTestCommit() = {
    val commit = jgit.commit()
    commit.setMessage("test commit")
    commit.call()
  }

  def close() = jgit.close()
}
