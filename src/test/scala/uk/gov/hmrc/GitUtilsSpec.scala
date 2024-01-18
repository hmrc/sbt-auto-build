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

import org.eclipse.jgit.lib.Repository
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GitUtilsSpec
  extends AnyWordSpec
     with GitRepository
     with Matchers
     with OptionValues {

  "remote url" should {
    "find the remote connection url for origin" in new Setup {
      gitHelper.setRemote("origin", "git@github.com:origin/non-existent.git")

      gitHelper.createTestCommit()

      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"
    }

    "find the remote connection url for origin when multiple remotes set" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git@github.com:origin/non-existent.git")
      gitHelper.setRemoteWithBranch("remote1", "branch1", "git@github.com:remote1/non-existent.git")
      gitHelper.setRemoteWithBranch("remote2", "branch2", "git@github.com:remote2/non-existent.git")

      gitHelper.createTestCommit()
      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"

      gitHelper.checkoutNewBranch("branch1")
      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"

      gitHelper.checkoutNewBranch("branch2")
      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"
    }

    "find the remote connection url defaulted to the origin remote when main branch does not exist" in new Setup {
      gitHelper.setRemote("origin","git@github.com:origin/non-existent.git")

      val rev = gitHelper.createTestCommit()
      gitHelper.checkoutBranch(rev)
      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"
    }

    "find the remote connection url when other branches have no url set" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git@github.com:origin/non-existent.git")

      // One possible situation this occurs is if there is a remote section in the global config,
      // for example, to set the refspec or other properties globally, but that remote doesn't exist in the
      // current repository
      gitHelper.setRemoteWithBranch("remote1", "branch1", null)
      gitHelper.setRemoteWithBranch("remote2", "branch2", "git@github.com:remote2/non-existent.git")

      gitHelper.createTestCommit()

      gitHelper.checkoutNewBranch("branch1")
      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"

      gitHelper.checkoutNewBranch("branch2")

      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"
    }

    "return None when origin is not set" in new Setup {
      gitUtils.remoteConnectionUrl shouldBe None

      gitHelper.setRemoteWithBranch("upstream", "main", "git@github.com:origin/non-existent.git")

      gitUtils.remoteConnectionUrl shouldBe None
    }

    "transform remote connection url with git:// to git@" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git://github.com:hmrc/sbt-auto-build1.git")
      gitUtils.remoteConnectionUrl.value shouldBe "git@github.com:hmrc/sbt-auto-build1.git"
    }
  }

  "create the browser url" should {
    "be created when connection url starting with 'git@'" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git@github.com:hmrc/sbt-auto-build.git")
      gitUtils.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url starting with 'git@' on a fork" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git@github.com:hmrc-collaborator/sbt-auto-build.git")
      gitUtils.browserUrl.value shouldBe "https://github.com/hmrc-collaborator/sbt-auto-build"
    }

    "be created when connection url starting with 'https://" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "https://github.com/hmrc/sbt-auto-build.git")
      gitUtils.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url starting with 'git://'" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git://github.com:hmrc/sbt-auto-build.git")
      gitUtils.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url has repo organisation in capitals" in new Setup {
      gitHelper.setRemoteWithBranch("origin", "main", "git://github.com:HMRC/sbt-auto-build.git")
      gitUtils.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }
  }

  trait Setup {
    // GitRepository creates a new GitHelper per test, so we require new GitUtils
    val gitUtils: GitUtils =
      new GitUtils {
        override val repository: Repository =
          gitHelper.repo
      }
  }
}
