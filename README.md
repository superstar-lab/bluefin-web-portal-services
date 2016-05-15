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
