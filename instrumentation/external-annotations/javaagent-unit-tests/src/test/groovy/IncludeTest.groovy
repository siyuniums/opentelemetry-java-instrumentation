/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.javaagent.bootstrap.internal.InstrumentationConfig
import io.opentelemetry.javaagent.instrumentation.extannotations.ExternalAnnotationInstrumentation
import spock.lang.Specification
import spock.lang.Unroll

import static io.opentelemetry.javaagent.instrumentation.extannotations.ExternalAnnotationInstrumentation.DEFAULT_ANNOTATIONS

class IncludeTest extends Specification {

  @Unroll
  def "test configuration #value"() {
    setup:
    InstrumentationConfig config = Mock()
    config.getString("otel.instrumentation.external-annotations.include") >> value

    expect:
    ExternalAnnotationInstrumentation.configureAdditionalTraceAnnotations(config) == expected.toSet()

    where:
    value                               | expected
    null                                | DEFAULT_ANNOTATIONS.toList()
    " "                                 | []
    "some.Invalid[]"                    | []
    "some.package.ClassName "           | ["some.package.ClassName"]
    " some.package.Class\$Name"         | ["some.package.Class\$Name"]
    "  ClassName  "                     | ["ClassName"]
    "ClassName"                         | ["ClassName"]
    "Class\$1;Class\$2;"                | ["Class\$1", "Class\$2"]
    "Duplicate ;Duplicate ;Duplicate; " | ["Duplicate"]
  }

}
