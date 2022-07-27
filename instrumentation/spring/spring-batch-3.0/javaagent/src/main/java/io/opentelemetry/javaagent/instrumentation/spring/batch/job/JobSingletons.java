/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.spring.batch.job;

import static io.opentelemetry.javaagent.instrumentation.spring.batch.SpringBatchInstrumentationConfig.instrumentationName;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import org.springframework.batch.core.JobExecution;

public class JobSingletons {

  private static final Instrumenter<JobExecution, Void> INSTRUMENTER =
      Instrumenter.<JobExecution, Void>builder(
              GlobalOpenTelemetry.get(), instrumentationName(), JobSingletons::extractSpanName)
          .buildInstrumenter();

  private static String extractSpanName(JobExecution jobExecution) {
    return "BatchJob " + jobExecution.getJobInstance().getJobName();
  }

  public static Instrumenter<JobExecution, Void> jobInstrumenter() {
    return INSTRUMENTER;
  }

  private JobSingletons() {}
}
