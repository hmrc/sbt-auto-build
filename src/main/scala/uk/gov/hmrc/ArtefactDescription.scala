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

import sbt.Keys.{developers, homepage, organizationHomepage, pomPostProcess, scmInfo}
import sbt.{ScmInfo, url}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml._

object ArtefactDescription {

  def apply() = Seq(
    homepage := GitUtils.homepage,
    organizationHomepage := Some(url("https://www.gov.uk/government/organisations/hm-revenue-customs")),
    scmInfo := buildScmInfo,

    // workaround for sbt/sbt#1834
    pomPostProcess := {
      node: Node =>
        new RuleTransformer(new RewriteRule {
          override def transform(node: Node): NodeSeq = node match {
            case e: Elem if e.label == "developers" =>
              <developers>
                {developers.value.map { dev =>
                <developer>
                  <id>{dev.id}</id>
                  <name>{dev.name}</name>
                  <email>{dev.email}</email>
                  <url>{dev.url}</url>
                </developer>
              }}
              </developers>
            case _ => node
          }
        }).transform(node).head
    }
  )

  def buildScmInfo: Option[ScmInfo] = for {
      connUrl <- GitUtils.findRemoteConnectionUrl;
      browserUrl <- GitUtils.browserUrl
    } yield ScmInfo(url(browserUrl), connUrl)
}
