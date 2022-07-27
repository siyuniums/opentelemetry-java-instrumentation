# Supported libraries, frameworks, application servers, and JVMs

We automatically instrument and support a huge number of libraries, frameworks,
and application servers... right out of the box!

Don't see your favorite tool listed here?  Consider [filing an issue](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues),
or [contributing](../CONTRIBUTING.md).

## Contents

  * [Libraries / Frameworks](#libraries--frameworks)
  * [Application Servers](#application-servers)
  * [JVMs and Operating Systems](#jvms-and-operating-systems)
  * [Disabled instrumentations](#disabled-instrumentations)

## Libraries / Frameworks

These are the supported libraries and frameworks:

| Library/Framework                                                                                                                 | Versions                      |
|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| [Akka Actors](https://doc.akka.io/docs/akka/current/typed/index.html)                                                             | 2.5+                          |
| [Akka HTTP](https://doc.akka.io/docs/akka-http/current/index.html)                                                                | 10.0+                         |
| [Apache Axis2](https://axis.apache.org/axis2/java/core/)                                                                          | 1.6+                          |
| [Apache Camel](https://camel.apache.org/)                                                                                         | 2.20+ (not including 3.x yet) |
| [Apache CXF JAX-RS](https://cxf.apache.org/)                                                                                      | 3.2+                          |
| [Apache CXF JAX-RS Client](https://cxf.apache.org/)                                                                               | 3.0+                          |
| [Apache CXF JAX-WS](https://cxf.apache.org/)                                                                                      | 3.0+                          |
| [Apache Dubbo](https://github.com/apache/dubbo/)                                                                                  | 2.7+                          |
| [Apache HttpAsyncClient](https://hc.apache.org/index.html)                                                                        | 4.1+                          |
| [Apache HttpClient](https://hc.apache.org/index.html)                                                                             | 2.0+                          |
| [Apache Kafka Producer/Consumer API](https://kafka.apache.org/documentation/#producerapi)                                         | 0.11+                         |
| [Apache Kafka Streams API](https://kafka.apache.org/documentation/streams/)                                                       | 0.11+                         |
| [Apache MyFaces](https://myfaces.apache.org/)                                                                                     | 1.2+ (not including 3.x yet)  |
| [Apache RocketMQ](https://rocketmq.apache.org/)                                                                                   | 4.8+                          |
| [Apache Struts 2](https://github.com/apache/struts)                                                                               | 2.3+                          |
| [Apache Tapestry](https://tapestry.apache.org/)                                                                                   | 5.4+                          |
| [Apache Wicket](https://wicket.apache.org/)                                                                                       | 8.0+                          |
| [Armeria](https://armeria.dev)                                                                                                    | 1.3+                          |
| [AsyncHttpClient](https://github.com/AsyncHttpClient/async-http-client)                                                           | 1.9+                          |
| [AWS Lambda](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)                                                      | 1.0+                          |
| [AWS SDK](https://aws.amazon.com/sdk-for-java/)                                                                                   | 1.11.x and 2.2.0+             |
| [Azure Core](https://docs.microsoft.com/en-us/java/api/overview/azure/core-readme)                                                | 1.14+                         |
| [Cassandra Driver](https://github.com/datastax/java-driver)                                                                       | 3.0+                          |
| [Couchbase Client](https://github.com/couchbase/couchbase-java-client)                                                            | 2.0+ and 3.1+                 |
| [Dropwizard Metrics](https://metrics.dropwizard.io/)                                                                              | 4.0+ (disabled by default)    |
| [Dropwizard Views](https://www.dropwizard.io/en/latest/manual/views.html)                                                         | 0.7+                          |
| [Eclipse Grizzly](https://javaee.github.io/grizzly/httpserverframework.html)                                                      | 2.0+ (disabled by default)    |
| [Eclipse Jersey](https://eclipse-ee4j.github.io/jersey/)                                                                          | 2.0+ (not including 3.x yet)  |
| [Eclipse Jetty HTTP Client](https://www.eclipse.org/jetty/javadoc/jetty-9/org/eclipse/jetty/client/HttpClient.html)               | 9.2+ (not including 10+ yet)  |
| [Eclipse Metro](https://projects.eclipse.org/projects/ee4j.metro)                                                                 | 2.2+ (not including 3.x yet)  |
| [Eclipse Mojarra](https://projects.eclipse.org/projects/ee4j.mojarra)                                                             | 1.2+ (not including 3.x yet)  |
| [Elasticsearch API](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html)                             | 5.0+                          |
| [Elasticsearch REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)                    | 5.0+                          |
| [Finatra](https://github.com/twitter/finatra)                                                                                     | 2.9+                          |
| [Geode Client](https://geode.apache.org/)                                                                                         | 1.4+                          |
| [Google HTTP Client](https://github.com/googleapis/google-http-java-client)                                                       | 1.19+                         |
| [Grails](https://grails.org/)                                                                                                     | 3.0+                          |
| [GraphQL Java](https://www.graphql-java.com/)                                                                                     | 12.0+                         |
| [gRPC](https://github.com/grpc/grpc-java)                                                                                         | 1.6+                          |
| [Guava ListenableFuture](https://guava.dev/releases/snapshot/api/docs/com/google/common/util/concurrent/ListenableFuture.html)    | 10.0+                         |
| [GWT](http://www.gwtproject.org/)                                                                                                 | 2.0+                          |
| [Hibernate](https://github.com/hibernate/hibernate-orm)                                                                           | 3.3+                          |
| [HikariCP](https://github.com/brettwooldridge/HikariCP)                                                                           | 3.0+                          |
| [HttpURLConnection](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/HttpURLConnection.html)                 | Java 8+                       |
| [Hystrix](https://github.com/Netflix/Hystrix)                                                                                     | 1.4+                          |
| [Java Executors](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html)                                    | Java 8+                       |
| [Java Http Client](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html)           | Java 11+                      |
| [java.util.logging](https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html)                             | Java 8+                       |
| [JAX-RS](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/package-summary.html)                                          | 0.5+                          |
| [JAX-RS Client](https://javaee.github.io/javaee-spec/javadocs/javax/ws/rs/client/package-summary.html)                            | 1.1+                          |
| [JAX-WS](https://jakarta.ee/specifications/xml-web-services/2.3/apidocs/javax/xml/ws/package-summary.html)                        | 2.0+ (not including 3.x yet)  |
| [JBoss Log Manager](https://github.com/jboss-logging/jboss-logmanager)                                                             | 1.1+                          |
| [JDBC](https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html)                                                   | Java 8+                       |
| [Jedis](https://github.com/xetorthio/jedis)                                                                                       | 1.4+                          |
| [JMS](https://javaee.github.io/javaee-spec/javadocs/javax/jms/package-summary.html)                                               | 1.1+                          |
| [JSP](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/jsp/package-summary.html)                                       | 2.3+                          |
| [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)                                                         | 1.0+                          |
| [Kubernetes Client](https://github.com/kubernetes-client/java)                                                                    | 7.0+                          |
| [Lettuce](https://github.com/lettuce-io/lettuce-core)                                                                             | 4.0+                          |
| [Log4j 1](https://logging.apache.org/log4j/1.2/)                                                                                  | 1.2+                          |
| [Log4j 2](https://logging.apache.org/log4j/2.x/)                                                                                  | 2.11+                         |
| [Logback](http://logback.qos.ch/)                                                                                                 | 1.0+                          |
| [Micrometer](https://micrometer.io/)                                                                                              | 1.5+                          |
| [MongoDB Driver](https://mongodb.github.io/mongo-java-driver/)                                                                    | 3.1+                          |
| [Netty](https://github.com/netty/netty)                                                                                           | 3.8+                          |
| [OkHttp](https://github.com/square/okhttp/)                                                                                       | 2.2+                          |
| [Oracle UCP](https://docs.oracle.com/database/121/JJUCP/)                                                                         | 11.2+                         |
| [OSHI](https://github.com/oshi/oshi/)                                                                                             | 5.3.1+                        |
| [Play](https://github.com/playframework/playframework)                                                                            | 2.4+                          |
| [Play WS](https://github.com/playframework/play-ws)                                                                               | 1.0+                          |
| [Quartz](https://www.quartz-scheduler.org/)                                                                                       | 2.0+                          |
| [RabbitMQ Client](https://github.com/rabbitmq/rabbitmq-java-client)                                                               | 2.7+                          |
| [Ratpack](https://github.com/ratpack/ratpack)                                                                                     | 1.4+                          |
| [Reactor](https://github.com/reactor/reactor-core)                                                                                | 3.1+                          |
| [Reactor Netty](https://github.com/reactor/reactor-netty)                                                                         | 0.9+                          |
| [Rediscala](https://github.com/etaty/rediscala)                                                                                   | 1.8+                          |
| [Redisson](https://github.com/redisson/redisson)                                                                                  | 3.0+                          |
| [RESTEasy](https://resteasy.github.io/)                                                                                           | 3.0+                          |
| [Restlet](https://restlet.talend.com/)                                                                                            | 1.0+                          |
| [RMI](https://docs.oracle.com/en/java/javase/11/docs/api/java.rmi/java/rmi/package-summary.html)                                  | Java 8+                       |
| [RxJava](https://github.com/ReactiveX/RxJava)                                                                                     | 1.0+                          |
| [Scala ForkJoinPool](https://www.scala-lang.org/api/2.12.0/scala/concurrent/forkjoin/package$$ForkJoinPool$.html)                 | 2.8+                          |
| [Servlet](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/package-summary.html)                                       | 2.2+                          |
| [Spark Web Framework](https://github.com/perwendel/spark)                                                                         | 2.3+                          |
| [Spring Batch](https://spring.io/projects/spring-batch)                                                                           | 3.0+                          |
| [Spring Data](https://spring.io/projects/spring-data)                                                                             | 1.8+                          |
| [Spring Integration](https://spring.io/projects/spring-integration)                                                               | 4.1+                          |
| [Spring Kafka](https://spring.io/projects/spring-kafka)                                                                           | 2.7+                          |
| [Spring RabbitMQ](https://spring.io/projects/spring-amqp)                                                                         | 1.0+                          |
| [Spring Scheduling](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/package-summary.html)   | 3.1+                          |
| [Spring Web MVC](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/mvc/package-summary.html) | 3.1+                          |
| [Spring Web Services](https://spring.io/projects/spring-ws)                                                                       | 2.0+                          |
| [Spring WebFlux](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/reactive/package-summary.html)    | 5.0+                          |
| [Spymemcached](https://github.com/couchbase/spymemcached)                                                                         | 2.12+                         |
| [Tomcat JDBC Pool](https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html)                                                       | 8.5.0+                        |
| [Twilio](https://github.com/twilio/twilio-java)                                                                                   | 6.6+ (not including 8.x yet)  |
| [Undertow](https://undertow.io/)                                                                                                  | 1.4+                          |
| [Vaadin](https://vaadin.com/)                                                                                                     | 14.2+                         |
| [Vert.x Web](https://vertx.io/docs/vertx-web/java/)                                                                               | 3.0+                          |
| [Vert.x HttpClient](https://vertx.io/docs/apidocs/io/vertx/core/http/HttpClient.html)                                             | 3.0+                          |
| [Vert.x Kafka Client](https://vertx.io/docs/vertx-kafka-client/java/)                                                             | 3.6+                          |
| [Vert.x RxJava2](https://vertx.io/docs/vertx-rx/java2/)                                                                           | 3.5+                          |
| [Vibur DBCP](https://www.vibur.org/)                                                                                              | 11.0+                         |

## Application Servers

These are the application servers that the smoke tests are run against:

| Application server                                                                        | Version                     | JVM               | OS                             |
| ----------------------------------------------------------------------------------------- | --------------------------- | ----------------- | ------------------------------ |
| [Jetty](https://www.eclipse.org/jetty/)                                                   | 9.4.x, 10.0.x, 11.0.x       | OpenJDK 8, 11, 17 | Ubuntu 18, Windows Server 2019 |
| [Payara](https://www.payara.fish/)                                                        | 5.0.x, 5.1.x                | OpenJDK 8, 11     | Ubuntu 18, Windows Server 2019 |
| [Tomcat](http://tomcat.apache.org/)                                                       | 7.0.x                       | OpenJDK 8         | Ubuntu 18, Windows Server 2019 |
| [Tomcat](http://tomcat.apache.org/)                                                       | 7.0.x, 8.5.x, 9.0.x, 10.0.x | OpenJDK 8, 11, 17 | Ubuntu 18, Windows Server 2019 |
| [TomEE](https://tomee.apache.org/)                                                        | 7.x, 8.x                    | OpenJDK 8, 11, 17 | Ubuntu 18, Windows Server 2019 |
| [Websphere Liberty Profile](https://www.ibm.com/cloud/websphere-liberty)                  | 20.x, 21.x                  | OpenJDK 8         | Ubuntu 18, Windows Server 2019 |
| [Websphere Traditional](https://www.ibm.com/cloud/websphere-application-server)           | 8.5.5.x, 9.0.x              | IBM JDK 8         | Red Hat Enterprise Linux 8.4   |
| [WildFly](https://www.wildfly.org/)                                                       | 13.x                        | OpenJDK 8         | Ubuntu 18, Windows Server 2019 |
| [WildFly](https://www.wildfly.org/)                                                       | 17.x, 21.x, 25.x            | OpenJDK 8, 11, 17 | Ubuntu 18, Windows Server 2019 |

## JVMs and operating systems

These are the JVMs and operating systems that the integration tests are run against:

| JVM                                                                                        | Versions  | OS                             |
| ------------------------------------------------------------------------------------------ | --------- | ------------------------------ |
| [OpenJDK (Eclipse Temurin)](https://adoptium.net/)                                         | 8, 11, 17 | Ubuntu 18, Windows Server 2019 |
| [OpenJ9 (IBM Semeru Runtimes)](https://developer.ibm.com/languages/java/semeru-runtimes/)  | 8, 11, 17 | Ubuntu 18, Windows Server 2019 |

## Disabled instrumentations

Some instrumentations can produce too many spans and make traces very noisy.
For this reason, the following instrumentations are disabled by default:

- `jdbc-datasource` which creates spans whenever the `java.sql.DataSource#getConnection` method is called.
- `dropwizard-metrics` which might create a very low quality metrics data, because of lack of label/attribute support
  in the Dropwizard metrics API.

To enable them, add the `otel.instrumentation.<name>.enabled` system property:
`-Dotel.instrumentation.jdbc-datasource.enabled=true`
