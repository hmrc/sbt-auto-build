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
import uk.gov.hmrc.RepositoryYamlUtils.{Private, Public}

class RepositoryYamlUtilsSpec extends AnyWordSpec with Matchers {

   "loadRepositoryYamlFile" should {
     "return an error if the file does not exist" in {
       val missingFile = new File("missing")
       RepositoryYamlUtils.loadRepositoryYamlFile(missingFile) shouldBe Left(s"Unable to find repository.yaml file at ${missingFile.getAbsolutePath}/repository.yaml")
     }
   }

  "loadYaml" should {
    "return an error if the file does not contain valid yaml" in {
      RepositoryYamlUtils.loadYaml("garbage") shouldBe Left(s"File was not valid YAML")
    }
    "return a map of keys to values if valid yaml" in {
      RepositoryYamlUtils.loadYaml(s"repoVisibility: ${Public.visibilityIdentifier}\nanotherKey: something") shouldBe
        Right(Map("repoVisibility" -> Public.visibilityIdentifier, "anotherKey" -> "something"))
    }
  }

  "getRepoVisbility" should {
    "return public if the public visibility key is present" in {
      RepositoryYamlUtils.getRepoVisiblity(Map("repoVisibility" -> Public.visibilityIdentifier)) shouldBe Right(Public)
    }
    "return private if the private visibility key is present" in {
      RepositoryYamlUtils.getRepoVisiblity(Map("repoVisibility" -> Private.visibilityIdentifier)) shouldBe Right(Private)
    }
    "return an error if the repoVisibility identifier is not known" in {
      RepositoryYamlUtils.getRepoVisiblity(Map("repoVisibility" -> Private.visibilityIdentifier.dropRight(1))) shouldBe
        Left("The 'repoVisibility' identifier in repository.yaml is invalid. See https://confluence.tools.tax.service.gov.uk/x/k_8TCQ")
    }
    "return an error if the repoVisibility key is not present" in {
      RepositoryYamlUtils.getRepoVisiblity(Map.empty) shouldBe
        Left("The repository.yaml file in the root of the build is missing the 'repoVisibility' key")
    }
  }

}
