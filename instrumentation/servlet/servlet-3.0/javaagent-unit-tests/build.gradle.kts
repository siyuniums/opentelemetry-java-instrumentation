plugins {
  id("otel.java-conventions")
}

dependencies {
  testImplementation(project(":instrumentation:servlet:servlet-3.0:javaagent"))
}
