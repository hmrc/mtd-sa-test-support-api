package uk.gov.hmrc.internaltestsupport.connectors

import api.connectors.ConnectorSpec
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.internaltestsupport.models.MtdIdReference

class BusinessDetailsConnectorSpec extends ConnectorSpec {

  private val expectedId: String = "an expected Id"
  private val nino: String = "AA123456A"

  "Calling getMtdId with a NINO" should {
    "send a request and return the expected response" in new ConnectorTest {

  }

}
