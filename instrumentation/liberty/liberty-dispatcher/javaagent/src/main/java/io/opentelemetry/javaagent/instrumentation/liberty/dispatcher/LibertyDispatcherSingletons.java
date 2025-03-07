/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.liberty.dispatcher;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpRouteHolder;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanStatusExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.net.NetServerAttributesExtractor;
import io.opentelemetry.javaagent.bootstrap.internal.CommonConfig;

public final class LibertyDispatcherSingletons {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.liberty-dispatcher";

  private static final Instrumenter<LibertyRequest, LibertyResponse> INSTRUMENTER;

  static {
    LibertyDispatcherHttpAttributesGetter httpAttributesGetter =
        new LibertyDispatcherHttpAttributesGetter();
    LibertyDispatcherNetAttributesGetter netAttributesGetter =
        new LibertyDispatcherNetAttributesGetter();

    INSTRUMENTER =
        Instrumenter.<LibertyRequest, LibertyResponse>builder(
                GlobalOpenTelemetry.get(),
                INSTRUMENTATION_NAME,
                HttpSpanNameExtractor.create(httpAttributesGetter))
            .setSpanStatusExtractor(HttpSpanStatusExtractor.create(httpAttributesGetter))
            .addAttributesExtractor(
                HttpServerAttributesExtractor.builder(httpAttributesGetter)
                    .setCapturedRequestHeaders(CommonConfig.get().getServerRequestHeaders())
                    .setCapturedResponseHeaders(CommonConfig.get().getServerResponseHeaders())
                    .build())
            .addAttributesExtractor(NetServerAttributesExtractor.create(netAttributesGetter))
            .addContextCustomizer(HttpRouteHolder.get())
            .addOperationMetrics(HttpServerMetrics.get())
            .buildServerInstrumenter(LibertyDispatcherRequestGetter.INSTANCE);
  }

  public static Instrumenter<LibertyRequest, LibertyResponse> instrumenter() {
    return INSTRUMENTER;
  }

  private LibertyDispatcherSingletons() {}
}
