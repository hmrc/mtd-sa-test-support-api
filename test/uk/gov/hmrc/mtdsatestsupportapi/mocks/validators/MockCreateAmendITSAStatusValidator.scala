package uk.gov.hmrc.mtdsatestsupportapi.mocks.validators

import api.models.errors.MtdError
import org.scalamock.clazz.MockImpl.mock
import org.scalamock.handlers.CallHandler
import uk.gov.hmrc.mtdsatestsupportapi.controllers.requestParsers.validators.CreateAmendITSAStatusValidator
import uk.gov.hmrc.mtdsatestsupportapi.models.request.createAmendITSAStatus.CreateAmendITSAStatusRawData

trait MockCreateAmendITSAStatusValidator {

  val mockValidator: CreateAmendITSAStatusValidator = mock[CreateAmendITSAStatusValidator]

  object MockCreateAmendITSAStatusValidator {

    def validate(data: CreateAmendITSAStatusRawData): CallHandler[List[MtdError]] = {
      (mockValidator
        .validate(_: CreateAmendITSAStatusRawData))
        .expects(data)
    }

  }

}
