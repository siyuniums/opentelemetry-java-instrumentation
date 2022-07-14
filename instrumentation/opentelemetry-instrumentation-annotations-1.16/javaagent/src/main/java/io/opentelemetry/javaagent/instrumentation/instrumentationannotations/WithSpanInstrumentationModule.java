/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.instrumentationannotations;

import static java.util.Collections.singletonList;

import application.io.opentelemetry.instrumentation.annotations.WithSpan;
import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import java.util.List;

/** Instrumentation for methods annotated with {@link WithSpan} annotation. */
@AutoService(InstrumentationModule.class)
public class WithSpanInstrumentationModule extends InstrumentationModule {

  public WithSpanInstrumentationModule() {
    super("opentelemetry-instrumentation-annotations");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return singletonList(new WithSpanInstrumentation());
  }
}
