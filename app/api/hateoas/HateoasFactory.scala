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
import cats.implicits._
import config.AppConfig

import javax.inject.Inject

class HateoasFactory @Inject() (appConfig: AppConfig) {

  def wrap[A, D <: HateoasData](payload: A, data: D)(implicit lf: HateoasLinksFactory[A, D]): HateoasWrapper[A] = {
    val links = lf.links(appConfig, data)

    HateoasWrapper(payload, links)
  }

  def wrapList[A[_]: Functor, I, D](payload: A[I], data: D)(implicit lf: HateoasListLinksFactory[A, I, D]): HateoasWrapper[A[HateoasWrapper[I]]] = {
    val hateoasList = payload.map(i => HateoasWrapper(i, lf.itemLinks(appConfig, data, i)))

    HateoasWrapper(hateoasList, lf.links(appConfig, data))
  }

}

trait HateoasLinksFactory[A, D] {
  def links(appConfig: AppConfig, data: D): Seq[Link]
}

trait HateoasListLinksFactory[A[_], I, D] {
  def itemLinks(appConfig: AppConfig, data: D, item: I): Seq[Link]
  def links(appConfig: AppConfig, data: D): Seq[Link]
}
