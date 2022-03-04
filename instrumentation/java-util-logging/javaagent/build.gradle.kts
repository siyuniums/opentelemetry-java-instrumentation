plugins {
  id("otel.javaagent-instrumentation")
}

dependencies {
  compileOnly(project(":instrumentation:java-util-logging:shaded-stub-for-instrumenting"))

  compileOnly(project(":instrumentation-appender-api-internal"))

  // the JBoss instrumentation in this artifact is needed
  // for jboss-logmanager versions 1.1.0.GA through latest 2.x
  testLibrary("org.jboss.logmanager:jboss-logmanager:1.1.0.GA")

  testImplementation("org.awaitility:awaitility")
}

tasks.withType<Test>().configureEach {
  // TODO run tests both with and without experimental log attributes
  jvmArgs("-Dotel.instrumentation.java-util-logging.experimental-log-attributes=true")
}
