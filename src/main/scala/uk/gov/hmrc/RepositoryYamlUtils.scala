/*
 * Copyright 2020 HM Revenue & Customs
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

import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._
import scala.util.Try

object RepositoryYamlUtils {
  // IntelliJ tries to remove this on import cleanup, but is required when cross building for sbt 0.13
  import Extensions.RichTry

  val repositoryYamlFile = "repository.yaml"
  val repoVisibilityKey = "repoVisibility"

  type YAML = Map[String, String]

  sealed trait RepoVisibility {
    val visibilityIdentifier: String
  }
  case object Private extends RepoVisibility {
    override val visibilityIdentifier: String = "private_12E5349CFB8BBA30AF464C24760B70343C0EAE9E9BD99156345DD0852C2E0F6F"
  }
  case object Public extends RepoVisibility {
    override val visibilityIdentifier: String = "public_0C3F0CE3E6E6448FAD341E7BFA50FCD333E06A20CFF05FCACE61154DDBBADF71"
  }

  def loadRepositoryYamlFile(dir: File): Either[String, String] = {
    val yamlFile = dir.toPath.resolve(repositoryYamlFile).toFile

    FileUtils.readFileAsString(yamlFile).toEither.left.map(_ => s"Unable to find ${repositoryYamlFile} file at ${yamlFile.getAbsolutePath}. See https://confluence.tools.tax.service.gov.uk/x/k_8TCQ")
  }

  def loadYaml(yamlString: String): Either[String, YAML] = {
    Try{
      new Yaml()
        .load(yamlString)
        .asInstanceOf[java.util.Map[String, String]]
        .asScala.toMap
    }.toEither.left.map(_ => s"The ${repositoryYamlFile} file was not valid YAML")
  }

  def getRepoVisiblity(yaml: YAML): Either[String, RepoVisibility] = {
    yaml.get(repoVisibilityKey) match {
      case Some(v) => v match {
        case Private.visibilityIdentifier => Right(Private)
        case Public.visibilityIdentifier => Right(Public)
        case _ => Left(s"The '${repoVisibilityKey}' identifier in ${repositoryYamlFile} is invalid. See https://confluence.tools.tax.service.gov.uk/x/k_8TCQ")
      }
      case _ => Left(s"The ${repositoryYamlFile} file in the root of the build is missing the '${repoVisibilityKey}' key")
    }
  }

}
