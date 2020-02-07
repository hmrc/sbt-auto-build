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

import uk.gov.hmrc.RepositoryYamlUtils.{Private, Public, RepoVisibility}

object HeaderUtils {

  // IntelliJ tries to remove this on import cleanup, but is required when cross building for sbt 0.13
  import Extensions.RichTry

  val licenceFile = new File("LICENSE")
  val repositoryYamlFile = new File("repository.yaml")
  // Standard Apache v2 LICENSE (same as the one in this repo), after being trimmed of leading and trailing whitespace.
  // The reason for that is the official Apache licence has a beginning blank line, whereas the copy in most hmrc repo's
  // does not have that.
  val licenceFileExpectedMD5 = "cc1a9e33dd7a6eb0b79927742cf005c"
  val licenceFileExpectedMD5_2 = "6c4db32a2fa8717faffa1d4f10136f47" //Older variant with {} replaced by []

  def md5HashString(s: String): String = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1,digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  def checkLicenceFile(repoVisibility: RepoVisibility): Either[String, Unit] = repoVisibility match {
    case Private =>
      if(licenceFile.exists()) Left(s"LICENSE file exists but the repository is marked as private. Please remove it")
      else Right(())
    case Public =>
      if (licenceFile.exists()) {
        FileUtils.readFileAsString(licenceFile).toEither.left.map(_ => s"Problem reading LICENSE file")
          // .right projection required to remain backwards compatible with scala 2.10 cross build (for sbt 0.13)
          .right.flatMap(c =>
          if (Seq(licenceFileExpectedMD5, licenceFileExpectedMD5_2).contains(md5HashString(c.trim))) Right(())
          else Left(s"The LICENSE file does not contain the appropriate Apache V2 licence. It should match https://www.apache.org/licenses/LICENSE-2.0.txt")
        )
      }
      else Left(s"No LICENSE file exists but the repository is marked as public")
  }

  def shouldGenerateHeaders(force: Boolean): Boolean = {

    // .right projection required to remain backwards compatible with scala 2.10 cross build (for sbt 0.13)
    lazy val errorOrVisibility = for {
      yamlString <- RepositoryYamlUtils.loadRepositoryYamlFile(new File("")).right
      yaml <- RepositoryYamlUtils.loadYaml(yamlString).right
      repoVisibility <- RepositoryYamlUtils.getRepoVisiblity(yaml).right
      _ <- checkLicenceFile(repoVisibility).right
    } yield repoVisibility

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
