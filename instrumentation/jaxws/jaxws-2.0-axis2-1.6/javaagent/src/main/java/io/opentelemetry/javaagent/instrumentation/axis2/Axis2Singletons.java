/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.axis2;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.javaagent.bootstrap.internal.ExperimentalConfig;

public class Axis2Singletons {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.jaxws-2.0-axis2-1.6";

  private static final Instrumenter<Axis2Request, Void> INSTRUMENTER;

  static {
    INSTRUMENTER =
        Instrumenter.<Axis2Request, Void>builder(
                GlobalOpenTelemetry.get(), INSTRUMENTATION_NAME, Axis2Request::spanName)
            .setEnabled(ExperimentalConfig.get().controllerTelemetryEnabled())
            .buildInstrumenter();
  }

  public static Instrumenter<Axis2Request, Void> instrumenter() {
    return INSTRUMENTER;
  }

  private Axis2Singletons() {}
}
