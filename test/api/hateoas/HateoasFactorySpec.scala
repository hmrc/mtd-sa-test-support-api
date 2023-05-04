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

import api.models.hateoas.{HateoasData, HateoasWrapper, Link}
import api.models.hateoas.Method.GET
import cats.Functor
import config.AppConfig
import mocks.MockAppConfig
import support.UnitSpec

class HateoasFactorySpec extends UnitSpec with MockAppConfig {

  val hateoasFactory = new HateoasFactory(mockAppConfig)

  case class Response(foo: String)
  case class ListResponse[A](items: Seq[A])

  case class Data1(id: String) extends HateoasData
  case class Data2(id: String) extends HateoasData

  val response: Response = Response("X")

  class Test {
    MockAppConfig.apiGatewayContext.returns("context").anyNumberOfTimes
  }

  "wrap" should {

    implicit object LinksFactory1 extends HateoasLinksFactory[Response, Data1] {
      override def links(appConfig: AppConfig, data: Data1): Seq[Link] = Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}", GET, "rel1"))
    }

    implicit object LinksFactory2 extends HateoasLinksFactory[Response, Data2] {
      override def links(appConfig: AppConfig, data: Data2): Seq[Link] = Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}", GET, "rel2"))
    }

    "use the response specific links" in new Test {
      hateoasFactory.wrap(response, Data1("id")) shouldBe HateoasWrapper(response, Seq(Link("context/id", GET, "rel1")))
    }

    "use the endpoint HateoasData specific links" in new Test {
      hateoasFactory.wrap(response, Data2("id")) shouldBe HateoasWrapper(response, Seq(Link("context/id", GET, "rel2")))
    }
  }

  "wrapList" should {

    implicit object ListResponseFunctor extends Functor[ListResponse] {
      override def map[A, B](fa: ListResponse[A])(f: A => B): ListResponse[B] = ListResponse(fa.items.map(f))
    }

    implicit object LinksFactory extends HateoasListLinksFactory[ListResponse, Response, Data1] {
      override def itemLinks(appConfig: AppConfig, data: Data1, item: Response): Seq[Link] =
        Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}/${item.foo}", GET, "item"))

      override def links(appConfig: AppConfig, data: Data1): Seq[Link] = Seq(Link(s"${appConfig.apiGatewayContext}/${data.id}", GET, "rel"))
    }

    "work" in new Test {
      hateoasFactory.wrapList(ListResponse(Seq(response)), Data1("id")) shouldBe
        HateoasWrapper(ListResponse(Seq(HateoasWrapper(response, Seq(Link("context/id/X", GET, "item"))))), Seq(Link("context/id", GET, "rel")))
    }
  }
}
