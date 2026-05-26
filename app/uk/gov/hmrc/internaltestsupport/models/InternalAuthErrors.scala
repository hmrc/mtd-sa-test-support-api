/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.internaltestsupport.models

import api.models.errors.MtdError
import play.api.http.Status.UNPROCESSABLE_ENTITY

object OAuthCodeRetrievalError extends MtdError("OAUTH_CODE_RETRIEVAL_ERROR", "Failed to retrieve OAuth code from GG", UNPROCESSABLE_ENTITY)

object GrantScopeRetrievalError extends MtdError("GRANTSCOPE_NOT_FOUND", "Failed to retrieve grant scope from GG", UNPROCESSABLE_ENTITY)

object PWTimeoutError extends MtdError("PW_TIMEOUT", "", UNPROCESSABLE_ENTITY) {
  def withMessage(message: String): MtdError = this.copy(message = message)
}

object PWError extends MtdError("PLAYWRIGHT_ERROR", "", UNPROCESSABLE_ENTITY) {
  def withMessage(message: String): MtdError = this.copy(message = message)
}
