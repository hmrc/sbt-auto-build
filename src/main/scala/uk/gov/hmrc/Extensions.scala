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

import scala.util.{Success, Try}

object Extensions {

  // Try.toEither is not available for scala 2.10 used for the 0.13.x cross build, so use transform instead
  implicit class RichTry[T](t:Try[T]){
    def toEither: Either[Throwable,T] = t.transform(s => Success(Right(s)), f => Success(Left(f))).get
  }

}
