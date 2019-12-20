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

import uk.gov.hmrc.RepositoryYamlUtils.{Private, Public, RepoVisibility}

import scala.io.Source
import scala.util.Try

object HeaderUtils {

  // IntelliJ tries to remove this on import cleanup, but is required when cross building for sbt 0.13
  import Extensions.RichTry

  val license = new File("LICENSE")
  val repositoryYamlFile = new File("repository.yaml")

  val expectedLicenceText =
    """Apache License
      |                           Version 2.0, January 2004""".stripMargin

  def shouldGenerateHeaders(force: Boolean): Boolean = {

    // .right projection required to remain backwards compatible with scala 2.10 cross build (for sbt 0.13)
    lazy val errorOrVisibility = for {
      yamlString <- RepositoryYamlUtils.loadRepositoryYamlFile(new File("")).right
      yaml <- RepositoryYamlUtils.loadYaml(yamlString).right
      repoVisibility <- RepositoryYamlUtils.getRepoVisiblity(yaml).right
      _ <- checkLicenceFile(repoVisibility).right
    } yield repoVisibility

    def checkLicenceFile(repoVisibility: RepoVisibility): Either[String, Unit] = repoVisibility match {
      case Private =>
        if(license.exists()) Left(s"LICENSE file exists but the repository is marked as private. Please remove it")
        else Right(())
      case Public =>
        if (license.exists()) {
          Try {
            val f = Source.fromFile(license)
            val content = f.mkString
            f.close()
            content
          }.toEither.left.map(_ => s"Problem reading LICENSE file")
            // .right projection required to remain backwards compatible with scala 2.10 cross build (for sbt 0.13)
            .right.flatMap(c =>
            if (c.contains(expectedLicenceText)) Right(())
            else Left(s"The LICENSE file does not contain the appropriate Apache V2 licence")
          )
        }
        else Left(s"No LICENSE file exists but the repository is marked as public")
    }

    if (force) {
      SbtAutoBuildPlugin.logger.info(s"SbtAutoBuildPlugin - ${SbtAutoBuildPlugin.forceLicenceHeader.key.label} setting was set to true, Apache 2.0 licence file headers will be generated regardless")
      true
    } else errorOrVisibility match {
      case Right(Public) =>
        SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - repository is marked public. Licence headers will be added to all source files")
        true
      case Right(Private) =>
        SbtAutoBuildPlugin.logger.info("SbtAutoBuildPlugin - repository is marked private. No licence headers will be added to source files, only a copyright notice")
        false
      case Left(error) =>
        sys.error(s"SbtAutoBuildPlugin - Error, please fix: $error")
    }
  }

}
