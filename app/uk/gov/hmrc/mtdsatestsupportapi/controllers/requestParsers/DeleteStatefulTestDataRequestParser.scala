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

package uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.DeleteStatefulTestDataValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.deleteStatefulTestData.{DeleteStatefulTestDataRawData, DeleteStatefulTestDataRequest}

import javax.inject.Inject

class DeleteStatefulTestDataRequestParser @Inject() (val validator: DeleteStatefulTestDataValidator)
  extends RequestParser[DeleteStatefulTestDataRawData, DeleteStatefulTestDataRequest] {

  override protected def requestFor(data: DeleteStatefulTestDataRawData): DeleteStatefulTestDataRequest =
    DeleteStatefulTestDataRequest(data.vendorClientId, data.body)
}
