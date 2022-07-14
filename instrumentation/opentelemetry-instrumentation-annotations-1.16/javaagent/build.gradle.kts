plugins {
  id("otel.javaagent-instrumentation")
}

// note that muzzle is not run against the current SNAPSHOT instrumentation-annotations, but this is
// ok because the tests are run against the current SNAPSHOT instrumentation-annotations which will
// catch any muzzle issues in SNAPSHOT instrumentation-annotations

muzzle {
  pass {
    group.set("io.opentelemetry")
    module.set("opentelemetry-instrumentation-annotations")
    versions.set("(,)")
  }
}

val versions: Map<String, String> by project

dependencies {
  compileOnly(project(":instrumentation-annotations-support"))

  compileOnly(project(":javaagent-tooling"))

  // this instrumentation needs to do similar shading dance as opentelemetry-api-1.0 because
  // the @WithSpan annotation references the OpenTelemetry API's SpanKind class
  //
  // see the comment in opentelemetry-api-1.0.gradle for more details
  compileOnly(project(":opentelemetry-instrumentation-annotations-shaded-for-instrumenting", configuration = "shadow"))

  testImplementation("io.opentelemetry:opentelemetry-extension-annotations")
  testImplementation(project(":instrumentation-annotations-support"))
  testImplementation("net.bytebuddy:byte-buddy")
}

tasks {
  compileTestJava {
    options.compilerArgs.add("-parameters")
  }
  test {
    jvmArgs("-Dotel.instrumentation.opentelemetry-annotations.exclude-methods=io.opentelemetry.test.annotation.TracedWithSpan[ignored]")
  }
}
