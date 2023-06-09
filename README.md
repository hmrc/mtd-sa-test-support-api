
# mtd-sa-test-support-api

This API allows a developer to:
- delete stateful test data supplied by them in the sandbox environment

### Requirements 

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager](https://github.com/hmrc/service-manager)

### Development Setup

Run the microservice from the console using the command: `sbt run` (starts on port 12221 by default)

Start the service manager profile: `sm2 --start MTDFB_ALL`

### Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

### Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

### Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

### API Reference / Documentation
Available on the [HMRC Developer Hub](https://developer.staging.tax.service.gov.uk/api-documentation/docs/api/service/mtd-sa-test-support-api/1.0)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
