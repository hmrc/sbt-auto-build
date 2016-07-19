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

import org.scalatest.{OptionValues, ShouldMatchers, WordSpec}

class ArtefactDescriptionSpec extends WordSpec with ShouldMatchers with OptionValues{

  "remote url" should {
    "find the remote connection url" in {
      Git.findRemoteConnectionUrl.value should
        fullyMatch regex("(git@github.com:([^/]*)/sbt-auto-build.git)|(https://github.com/([^/]*)/sbt-auto-build.git)")
    }
  }

  "create the browser url" should {

    "be created when connection url starting with 'git@'" in {
      Git.browserUrl("git@github.com:hmrc/sbt-auto-build") shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url starting with 'git@' on a fork" in {
      Git.browserUrl("git@github.com:user/sbt-auto-build") shouldBe "https://github.com/user/sbt-auto-build"
    }

    "be created when connection url starting with 'git://'" in {
      Git.browserUrl("git://github.com:hmrc/sbt-auto-build") shouldBe "https://github.com/hmrc/sbt-auto-build"
    }

    "be created when connection url has repo organisation in capitals" in {
      Git.browserUrl("git://github.com:HMRC/sbt-auto-build") shouldBe "https://github.com/hmrc/sbt-auto-build"
      Git.browserUrl("git://github.com:hmrc/sbt-auto-build") shouldBe "https://github.com/hmrc/sbt-auto-build"
    }
  }
}
