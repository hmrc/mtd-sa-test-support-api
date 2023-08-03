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

package api.hateoas

import cats.Functor
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory

trait MockHateoasFactory extends MockFactory {

  val mockHateoasFactory: HateoasFactory = mock[HateoasFactory]

  object MockHateoasFactory {

    def wrap[A, D <: HateoasData](a: A, data: D): CallHandler[HateoasWrapper[A]] = {
      (mockHateoasFactory
        .wrap(_: A, _: D)(_: HateoasLinksFactory[A, D]))
        .expects(a, data, *)
    }

    def wrapList[A[_]: Functor, I, D <: HateoasData](a: A[I], data: D): CallHandler[HateoasWrapper[A[HateoasWrapper[I]]]] = {
      (mockHateoasFactory
        .wrapList(_: A[I], _: D)(_: Functor[A], _: HateoasListLinksFactory[A, I, D]))
        .expects(a, data, *, *)
    }

  }

}
