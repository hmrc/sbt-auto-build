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

import org.scalatest._


class ArtefactDescriptionSpec extends WordSpec with GitRepository with ShouldMatchers with OptionValues {

  "remote url" must {
    "find the remote connection url for the current branch with a single remote" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git@github.com:origin/non-existent.git")

      gitHelper.createTestCommit()

      git.findRemoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"
    }

    "find the remote connection url for the current branch with multiple remotes" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git@github.com:origin/non-existent.git")
      gitHelper.setRemoteWithBranch("remote1", "branch1", "git@github.com:remote1/non-existent.git")
      gitHelper.setRemoteWithBranch("remote2", "branch2", "git@github.com:remote2/non-existent.git")

      gitHelper.createTestCommit()

      git.findRemoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"

      gitHelper.checkoutNewBranch("branch1")

      git.findRemoteConnectionUrl.value shouldBe "git@github.com:remote1/non-existent.git"

      gitHelper.checkoutNewBranch("branch2")

      git.findRemoteConnectionUrl.value shouldBe "git@github.com:remote2/non-existent.git"
    }

    "find the remote connection url defaulted to the remote for master when in a branch without tracking info" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git@github.com:origin/non-existent.git")

      val rev = gitHelper.createTestCommit()

      gitHelper.checkoutBranch(rev)

      git.findRemoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"
    }

    "find the remote connection url when other branches have no url set" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git@github.com:origin/non-existent.git")

      // One possible situation this occurs is if there is a remote section in the global config,
      // for example, to set the refspec or other properties globally, but that remote doesn't exist in the
      // current repository
      gitHelper.setRemoteWithBranch("remote1", "branch1", null)
      gitHelper.setRemoteWithBranch("remote2", "branch2", "git@github.com:remote2/non-existent.git")

      gitHelper.createTestCommit()

      gitHelper.checkoutNewBranch("branch1")
      git.findRemoteConnectionUrl.value shouldBe "git@github.com:origin/non-existent.git"

      gitHelper.checkoutNewBranch("branch2")

      git.findRemoteConnectionUrl.value shouldBe "git@github.com:remote2/non-existent.git"
    }

    "transform remote connection url with git:// to git@" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git://github.com:hmrc/sbt-auto-build1.git")
      git.findRemoteConnectionUrl.value shouldBe "git@github.com:hmrc/sbt-auto-build1.git"
    }
  }

  "create the browser url" should {
    "be created when connection url starting with 'git@'" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git@github.com:hmrc/sbt-auto-build")
      git.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url starting with 'git@' on a fork" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git@github.com:hmrc-collaborator/sbt-auto-build")
      git.browserUrl.value shouldBe "https://github.com/hmrc-collaborator/sbt-auto-build"
    }

    "be created when connection url starting with 'git://'" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git://github.com:hmrc/sbt-auto-build")
      git.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url has repo organisation in capitals" in {
      gitHelper.setRemoteWithBranch("origin", "master", "git://github.com:HMRC/sbt-auto-build")
      git.browserUrl.value shouldBe "https://github.com/hmrc/sbt-auto-build"
    }
  }
}

