/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.graphql;

import graphql.ExecutionResult;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.SpanStatusExtractor;

@SuppressWarnings("AbbreviationAsWordInName")
public final class GraphQLTelemetry {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.graphql-java-12.0";

  /** Returns a new {@link GraphQLTelemetry} configured with the given {@link OpenTelemetry}. */
  public static GraphQLTelemetry create(OpenTelemetry openTelemetry) {
    return builder(openTelemetry).build();
  }

  /**
   * Returns a new {@link GraphQLTelemetryBuilder} configured with the given {@link OpenTelemetry}.
   */
  public static GraphQLTelemetryBuilder builder(OpenTelemetry openTelemetry) {
    return new GraphQLTelemetryBuilder(openTelemetry);
  }

  private final Instrumenter<InstrumentationExecutionParameters, ExecutionResult> instrumenter;
  private final boolean sanitizeQuery;

  GraphQLTelemetry(OpenTelemetry openTelemetry, boolean sanitizeQuery) {
    InstrumenterBuilder<InstrumentationExecutionParameters, ExecutionResult> builder =
        Instrumenter.<InstrumentationExecutionParameters, ExecutionResult>builder(
                openTelemetry, INSTRUMENTATION_NAME, ignored -> "GraphQL Operation")
            .setSpanStatusExtractor(
                (spanStatusBuilder, instrumentationExecutionParameters, executionResult, error) -> {
                  if (!executionResult.getErrors().isEmpty()) {
                    spanStatusBuilder.setStatus(StatusCode.ERROR);
                  } else {
                    SpanStatusExtractor.getDefault()
                        .extract(
                            spanStatusBuilder,
                            instrumentationExecutionParameters,
                            executionResult,
                            error);
                  }
                });
    builder.addAttributesExtractor(new GraphqlAttributesExtractor());

    this.instrumenter = builder.buildInstrumenter();
    this.sanitizeQuery = sanitizeQuery;
  }

  /**
   * Returns a new {@link Instrumentation} that generates telemetry for received GraphQL requests.
   */
  public Instrumentation newInstrumentation() {
    return new OpenTelemetryInstrumentation(instrumenter, sanitizeQuery);
  }
}
