/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.webmvc;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpRouteHolder;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesExtractorBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanStatusExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.net.NetServerAttributesExtractor;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** A builder of {@link SpringWebMvcTelemetry}. */
public final class SpringWebMvcTelemetryBuilder {

  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.spring-webmvc-3.1";

  private final OpenTelemetry openTelemetry;
  private final List<AttributesExtractor<HttpServletRequest, HttpServletResponse>>
      additionalExtractors = new ArrayList<>();
  private final HttpServerAttributesExtractorBuilder<HttpServletRequest, HttpServletResponse>
      httpAttributesExtractorBuilder =
          HttpServerAttributesExtractor.builder(SpringWebMvcHttpAttributesGetter.INSTANCE);

  SpringWebMvcTelemetryBuilder(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  /**
   * Adds an additional {@link AttributesExtractor} to invoke to set attributes to instrumented
   * items.
   */
  public SpringWebMvcTelemetryBuilder addAttributesExtractor(
      AttributesExtractor<HttpServletRequest, HttpServletResponse> attributesExtractor) {
    additionalExtractors.add(attributesExtractor);
    return this;
  }

  /**
   * Configures the HTTP request headers that will be captured as span attributes.
   *
   * @param requestHeaders A list of HTTP header names.
   */
  public SpringWebMvcTelemetryBuilder setCapturedRequestHeaders(List<String> requestHeaders) {
    httpAttributesExtractorBuilder.setCapturedRequestHeaders(requestHeaders);
    return this;
  }

  /**
   * Configures the HTTP response headers that will be captured as span attributes.
   *
   * @param responseHeaders A list of HTTP header names.
   */
  public SpringWebMvcTelemetryBuilder setCapturedResponseHeaders(List<String> responseHeaders) {
    httpAttributesExtractorBuilder.setCapturedResponseHeaders(responseHeaders);
    return this;
  }

  /**
   * Returns a new {@link SpringWebMvcTelemetry} with the settings of this {@link
   * SpringWebMvcTelemetryBuilder}.
   */
  public SpringWebMvcTelemetry build() {
    SpringWebMvcHttpAttributesGetter httpAttributesGetter =
        SpringWebMvcHttpAttributesGetter.INSTANCE;

    Instrumenter<HttpServletRequest, HttpServletResponse> instrumenter =
        Instrumenter.<HttpServletRequest, HttpServletResponse>builder(
                openTelemetry,
                INSTRUMENTATION_NAME,
                HttpSpanNameExtractor.create(httpAttributesGetter))
            .setSpanStatusExtractor(HttpSpanStatusExtractor.create(httpAttributesGetter))
            .addAttributesExtractor(httpAttributesExtractorBuilder.build())
            .addAttributesExtractor(new StatusCodeExtractor())
            .addAttributesExtractor(
                NetServerAttributesExtractor.create(new SpringWebMvcNetAttributesGetter()))
            .addAttributesExtractors(additionalExtractors)
            .addOperationMetrics(HttpServerMetrics.get())
            .addContextCustomizer(HttpRouteHolder.get())
            .buildServerInstrumenter(JavaxHttpServletRequestGetter.INSTANCE);

    return new SpringWebMvcTelemetry(instrumenter);
  }
}
