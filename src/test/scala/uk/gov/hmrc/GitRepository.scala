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

import java.nio.file.Path

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{Ref, Repository, StoredConfig}
import org.eclipse.jgit.revwalk.RevCommit
import org.scalatest.{BeforeAndAfterEach, Suite}

import scala.reflect.io.File
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicReference

trait GitRepository extends BeforeAndAfterEach { this: Suite =>

  private val gitHelperRef = new AtomicReference[GitHelper]()

  def gitHelper: GitHelper =
    gitHelperRef.get()

  override def beforeEach(): Unit = {
    val tempWorkDir = Files.createTempDirectory("git_test")
    gitHelperRef.set(new GitHelper(tempWorkDir))
    super.beforeEach() // To be stackable, must call super.beforeEach
  }

  override def afterEach(): Unit =
    try {
      super.afterEach() // To be stackable, must call super.afterEach
    } finally {
      gitHelper.close()
      assert(new File(gitHelper.tempWorkDir.toFile).deleteRecursively(), "failed to delete file")
    }
}

case class GitHelper(tempWorkDir: Path) {

  val jgit: Git =
    Git.init().setDirectory(tempWorkDir.toFile).call()

  val repo: Repository =
    jgit.getRepository

  val currentBranch: String =
    repo.getFullBranch

  val gitConfig: StoredConfig =
    repo.getConfig

  def setRemote(name: String, url: String): Unit = {
    gitConfig.setString("remote", name, "url", url)
    gitConfig.setString("remote", name, "fetch", s"+refs/heads/*:refs/remotes/$name/*")
  }

  def setBranch(name: String, remote: String): Unit = {
    gitConfig.setString("branch", name, "remote", remote)
    gitConfig.setString("branch", name, "merge", s"refs/heads/$name")
  }

  def setRemoteWithBranch(remote: String, branch: String, url: String): Unit = {
    setRemote(remote, url)
    setBranch(branch, remote)
  }

  def checkoutNewBranch(name: String): Ref = {
    val checkout = jgit.checkout()
    checkout.setName(name)
    checkout.setCreateBranch(true)
    checkout.call()
  }

  def checkoutBranch(commit: RevCommit): Ref = {
    val checkout = jgit.checkout()
    checkout.setStartPoint(commit)
    checkout.setCreateBranch(true)
    checkout.setName("test")
    checkout.call()
  }

  def createTestCommit(): RevCommit = {
    val commit = jgit.commit()
    commit.setMessage("test commit")
    commit.call()
  }

  def close(): Unit =
    jgit.close()
}
