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

import java.io.File

import org.scalatest.TryValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HeaderUtilsSpec extends AnyWordSpec with Matchers {

  "md5HashString" should {
    "return the correct MD5 hash of the licence file (older variant with {} instead of [])" in {
      val f = new File(getClass.getResource("/LICENSE_APACHE_V2_OLD").getPath)
      val content = FileUtils.readFileAsString(f)

      HeaderUtils.md5HashString(content.success.value.trim) shouldBe "6c4db32a2fa8717faffa1d4f10136f47"
    }
    "return the correct MD5 hash of the licence file (current)" in {
      val f = new File(getClass.getResource("/LICENSE_APACHE_V2_CURRENT").getPath)
      val content = FileUtils.readFileAsString(f)

      HeaderUtils.md5HashString(content.success.value.trim) shouldBe "cc1a9e33dd7a6eb0b79927742cf005c"
    }
  }

}
