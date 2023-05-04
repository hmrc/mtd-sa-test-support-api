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

package api.controllers

import api.hateoas.{HateoasFactory, HateoasLinksFactory, HateoasListLinksFactory}
import api.models.hateoas.{HateoasData, HateoasWrapper}
import api.models.request.RawData
import cats.Functor
import play.api.http.{HttpEntity, Status}
import play.api.libs.json.{Json, JsValue, Writes}
import play.api.mvc.{ResponseHeader, Result, Results}

import scala.language.higherKinds

case class ResultWrapper(httpStatus: Int, body: Option[JsValue]) {

  def asResult: Result = {
    body match {
      case Some(b) => Results.Status(httpStatus)(b)
      case None    => Result(header = ResponseHeader(httpStatus), body = HttpEntity.NoEntity)
    }
  }

}

trait ResultCreator[InputRaw <: RawData, Input, Output] {

  def createResult(raw: InputRaw, input: Input, output: Output): ResultWrapper
}

object ResultCreator {

  def noContent[InputRaw <: RawData, Input, Output](successStatus: Int = Status.NO_CONTENT): ResultCreator[InputRaw, Input, Output] =
    (_: InputRaw, _: Input, _: Output) => ResultWrapper(successStatus, None)

  def plainJson[InputRaw <: RawData, Input, Output](successStatus: Int = Status.OK)(implicit
      ws: Writes[Output]): ResultCreator[InputRaw, Input, Output] =
    (_: InputRaw, _: Input, output: Output) => ResultWrapper(successStatus, Some(Json.toJson(output)))

  def hateoasWrapping[InputRaw <: RawData, Input, Output, HData <: HateoasData](hateoasFactory: HateoasFactory, successStatus: Int = Status.OK)(
      data: (Input, Output) => HData)(implicit
      linksFactory: HateoasLinksFactory[Output, HData],
      writes: Writes[HateoasWrapper[Output]]): ResultCreator[InputRaw, Input, Output] =
    (_: InputRaw, input: Input, output: Output) => {
      val wrapped = hateoasFactory.wrap(output, data(input, output))

      ResultWrapper(successStatus, Some(Json.toJson(wrapped)))
    }

  def hateoasListWrapping[InputRaw <: RawData, Input, Output[_]: Functor, I, HData <: HateoasData](hateoasFactory: HateoasFactory,
                                                                                                   successStatus: Int = Status.OK)(
      data: (Input, Output[I]) => HData)(implicit
      linksFactory: HateoasListLinksFactory[Output, I, HData],
      writes: Writes[HateoasWrapper[Output[HateoasWrapper[I]]]]): ResultCreator[InputRaw, Input, Output[I]] =
    (_: InputRaw, input: Input, output: Output[I]) => {
      val wrapped = hateoasFactory.wrapList(output, data(input, output))

      ResultWrapper(successStatus, Some(Json.toJson(wrapped)))
    }

}
