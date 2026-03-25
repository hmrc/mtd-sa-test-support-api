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

package utils

import support.UnitSpec

class IdGeneratorSpec extends UnitSpec {

  val generator        = new IdGenerator
  val correlationRegex = "^[A-Za-z0-9\\-]{36}$"

  "IdGenerator" should {
    "generate a correlation id" when {
      "getCorrelationId is called" in {
        generator.getCorrelationId.matches(correlationRegex) shouldBe true
      }
    }
  }

}
