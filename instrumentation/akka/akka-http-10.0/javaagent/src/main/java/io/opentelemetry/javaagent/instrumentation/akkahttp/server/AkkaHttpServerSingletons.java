/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.akkahttp.server;

import akka.http.scaladsl.model.HttpRequest;
import akka.http.scaladsl.model.HttpResponse;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpRouteHolder;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanStatusExtractor;
import io.opentelemetry.javaagent.bootstrap.internal.CommonConfig;
import io.opentelemetry.javaagent.instrumentation.akkahttp.AkkaHttpUtil;

public class AkkaHttpServerSingletons {

  private static final Instrumenter<HttpRequest, HttpResponse> INSTRUMENTER;

  static {
    AkkaHttpServerAttributesGetter httpAttributesGetter = new AkkaHttpServerAttributesGetter();
    INSTRUMENTER =
        Instrumenter.<HttpRequest, HttpResponse>builder(
                GlobalOpenTelemetry.get(),
                AkkaHttpUtil.instrumentationName(),
                HttpSpanNameExtractor.create(httpAttributesGetter))
            .setSpanStatusExtractor(HttpSpanStatusExtractor.create(httpAttributesGetter))
            .addAttributesExtractor(
                HttpServerAttributesExtractor.builder(httpAttributesGetter)
                    .setCapturedRequestHeaders(CommonConfig.get().getServerRequestHeaders())
                    .setCapturedResponseHeaders(CommonConfig.get().getServerResponseHeaders())
                    .build())
            .addOperationMetrics(HttpServerMetrics.get())
            .addContextCustomizer(HttpRouteHolder.get())
            .buildServerInstrumenter(AkkaHttpServerHeaders.INSTANCE);
  }

  public static Instrumenter<HttpRequest, HttpResponse> instrumenter() {
    return INSTRUMENTER;
  }

  public static HttpResponse errorResponse() {
    return (HttpResponse) HttpResponse.create().withStatus(500);
  }
}
