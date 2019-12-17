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

import java.io.File

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.HeaderUtils.{Private, Public}

class HeaderUtilsSpec extends AnyWordSpec with Matchers {

   "loadRepositoryYamlFile" should {
     "return an error if the file does not exist" in {
       val missingFile = new File("missing")
       HeaderUtils.loadRepositoryYamlFile(missingFile) shouldBe Left(s"Unable to find repository.yaml file at ${missingFile.getAbsolutePath}/repository.yaml")
     }
   }

  "loadYaml" should {
    "return an error if the file does not contain valid yaml" in {
      HeaderUtils.loadYaml("garbage") shouldBe Left(s"File was not valid YAML")
    }
    "return a map of keys to values if valid yaml" in {
      HeaderUtils.loadYaml(s"repoVisibility: ${Public.key}\nanotherKey: something") shouldBe
        Right(Map("repoVisibility" -> Public.key, "anotherKey" -> "something"))
    }
  }

  "getRepoVisbility" should {
    "return public if the public visibility key is present" in {
      HeaderUtils.getRepoVisiblity(Map("repoVisibility" -> Public.key)) shouldBe Right(Public)
    }
    "return private if the private visibility key is present" in {
      HeaderUtils.getRepoVisiblity(Map("repoVisibility" -> Private.key)) shouldBe Right(Private)
    }
    "return an error if the repoVisibility identifier is not known" in {
      HeaderUtils.getRepoVisiblity(Map("repoVisibility" -> Private.key.dropRight(1))) shouldBe Left("repoVisibility key is invalid")
    }
    "return an error if the repoVisibility key is not present" in {
      HeaderUtils.getRepoVisiblity(Map.empty) shouldBe Left("yaml is missing key repoVisibility")
    }
  }

}
