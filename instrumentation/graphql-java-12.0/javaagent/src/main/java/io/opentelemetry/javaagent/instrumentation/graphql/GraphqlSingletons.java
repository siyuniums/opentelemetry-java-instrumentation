/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.graphql;

import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.graphql.GraphQLTelemetry;
import io.opentelemetry.javaagent.bootstrap.internal.InstrumentationConfig;
import java.util.ArrayList;
import java.util.List;

public final class GraphqlSingletons {

  private static final boolean QUERY_SANITIZATION_ENABLED =
      InstrumentationConfig.get()
          .getBoolean("otel.instrumentation.graphql.query-sanitizer.enabled", true);

  private static final GraphQLTelemetry TELEMETRY =
      GraphQLTelemetry.builder(GlobalOpenTelemetry.get())
          .setSanitizeQuery(QUERY_SANITIZATION_ENABLED)
          .build();

  private GraphqlSingletons() {}

  public static Instrumentation addInstrumentation(Instrumentation instrumentation) {
    Instrumentation ourInstrumentation = TELEMETRY.newInstrumentation();
    if (instrumentation == null) {
      return ourInstrumentation;
    }
    if (instrumentation.getClass() == ourInstrumentation.getClass()) {
      return instrumentation;
    }
    List<Instrumentation> instrumentationList = new ArrayList<>();
    if (instrumentation instanceof ChainedInstrumentation) {
      instrumentationList.addAll(((ChainedInstrumentation) instrumentation).getInstrumentations());
    } else {
      instrumentationList.add(instrumentation);
    }
    boolean containsOurInstrumentation =
        instrumentationList.stream().anyMatch(ourInstrumentation.getClass()::isInstance);
    if (!containsOurInstrumentation) {
      instrumentationList.add(0, ourInstrumentation);
    }
    return new ChainedInstrumentation(instrumentationList);
  }
}
