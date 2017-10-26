# Bluefin Web Portal [Services]

Bluefin Web Portal is an application that provides to the costumer the following options:

  - Sale
  - Refund
  - Void (By transaction ID)
  - Reporting
  - Support all legal entities and applications 

### Version
1.0.0

### Release Notes
##### v1.0.0 - May 2016
* #6086 - Create base project configuration
* #6354 - Administrators can register users
* #6353 - Reset user password
##### Bugs
* None

### Tech

Bluefin Web Portal uses a number of technologies to work properly:

* Java 8
* [Spring Boot] - makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".
* [Spring Data JPA] - Makes it easy to easily implement JPA based repositories.
* [Spring MVC] - An open source application framework provides model-view-controller architecture.
* [HikariCP] - A high-performance JDBC connection pool.
* [QueryDSL] - Unified queries for Java.
* [Swagger] - Drive your API documentation.
* [Apache CFX] - An open source services framework.
* [Log4j2] - Enable logging.
* [TestNG] - A testing framework inspired from JUnit and NUnit. 
* [Mockito] - A mocking framework that tastes really good. It lets you write beautiful tests with a clean & simple API. 

### Installation

You need Maven installed.

#### **Local** environment (default)

```sh
$ mvn clean package
```
```sh
$ mvn clean package -P local
```

#### **Development** environment
```sh
$ mvn clean package -P dev
```

#### **QA** environment
```sh
$ mvn clean package -P qa
```

#### **Production** environment
```sh
$ mvn clean package -P prod
```

License
----

© 2016 Encore Capital Group All Rights Reserved.



[git-repo-url]: <http://tfs-prd.internal.mcmcg.com:8080/tfs/Encore/ICO/_git/Bluefin-web-portal-services>
[Spring Boot]: <http://projects.spring.io/spring-boot/>
[Spring Data JPA]: <http://projects.spring.io/spring-data-jpa/>
[Spring MVC]: <https://spring.io/guides/gs/rest-service/>
[QueryDSL]: <http://www.querydsl.com/>
[HikariCP]: <https://brettwooldridge.github.io/HikariCP/>
[Swagger]: <http://swagger.io/>
[Log4j2]: <http://logging.apache.org/log4j/2.x/>
[Apache CFX]: <http://cxf.apache.org/>
[Mockito]: <http://mockito.org/>
[TestNG]: <http://testng.org/doc/index.html>

AWS Micro Services URLs

https://bluefin-dev.mcmcg.com/bluefin-wp-services/
https://bluefin-dev.mcmcg.com/bluefin-client-registration-service/
https://bluefin-dev.mcmcg.com/bluefin-security-token-service/
https://bluefin-dev.mcmcg.com/bluefin-security-token-validation/
https://bluefin-dev.mcmcg.com/BankRoutingKeyService/
https://bluefin-dev.mcmcg.com/ProcessorNameService/
https://bluefin-dev.mcmcg.com/ResponseCodeMappingService/
https://bluefin-dev.mcmcg.com/SaveRefundTransactionService/
https://bluefin-dev.mcmcg.com/SaveSaleTransactionService/
https://bluefin-dev.mcmcg.com/SaveVoidTransactionService/
https://bluefin-dev.mcmcg.com/StatusCodeMappingService/
https://bluefin-dev.mcmcg.com/bluefin-batch-process-services/
https://bluefin-dev.mcmcg.com/bluefin-bindb-services/
https://bluefin-dev.mcmcg.com/bluefin-card-vault-service/

https://bluefin-qa.mcmcg.com/bluefin-wp-services/
https://bluefin-qa.mcmcg.com/bluefin-client-registration-service/
https://bluefin-qa.mcmcg.com/bluefin-security-token-service/
https://bluefin-qa.mcmcg.com/bluefin-security-token-validation/
https://bluefin-qa.mcmcg.com/BankRoutingKeyService/
https://bluefin-qa.mcmcg.com/ProcessorNameService/
https://bluefin-qa.mcmcg.com/ResponseCodeMappingService/
https://bluefin-qa.mcmcg.com/SaveRefundTransactionService/
https://bluefin-qa.mcmcg.com/SaveSaleTransactionService/
https://bluefin-qa.mcmcg.com/SaveVoidTransactionService/
https://bluefin-qa.mcmcg.com/StatusCodeMappingService/
https://bluefin-qa.mcmcg.com/bluefin-batch-process-services/
https://bluefin-qa.mcmcg.com/bluefin-bindb-services/
https://bluefin-qa.mcmcg.com/bluefin-card-vault-service/

*********************************************************
**********************	TEST-CC/DC  **********************
	TEST CC NUMBERS
	4012888888881881
	4111111111111111
	5105105105105100
	5178059383129039-working with payscout- decline amount .02 .01
	
TEST CC NUMBERS
4012888888881881
4111111111111111
5105105105105100
TEST DC NUMBERS
5555555555554444
4242424242424242
520082828282
4000245091267832-DENIED
5178059383129039-working with payscout- decline amount .02 .01

Test Debit cards
5555555555554444
520082828282
5332480594083455
Visa Debit Card
4000225643176329
Mastercard Debit
5100057924108390
5326760507185572-working with Jetpay-decline with .01
***************************Other Misc
	keytool -importcert -alias  phxiodpsgwd01 -file phxiodpsgwd01.cer -v -keystore cacerts 
	
	enterprise architect path
	\\internal.mcmcg.com\shares\Information Technology - IGI\Share\Sparx_Keystore\Encore_Keystore
	
	https://secure.payscout.com/merchants/login.php?cookie_check=1&auth_error=0
	MCMtest/Pays123!

	$ git checkout email
	
// git command to override a branch to another

$ git checkout email
$ git tag old-email-branch # This is optional
$ git reset --hard staging
$
$ # Using a custom commit message for the merge below
$ git merge -m 'Merge -s our where _ours_ is the branch staging' -s ours origin/email
$ git push origin email