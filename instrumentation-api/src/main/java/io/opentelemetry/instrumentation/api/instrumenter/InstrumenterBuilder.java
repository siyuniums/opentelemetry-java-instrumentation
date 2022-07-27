/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.instrumentation.api.internal.ConfigPropertiesUtil;
import io.opentelemetry.instrumentation.api.internal.EmbeddedInstrumentationProperties;
import io.opentelemetry.instrumentation.api.internal.SpanKey;
import io.opentelemetry.instrumentation.api.internal.SpanKeyProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A builder of an {@link Instrumenter}.
 *
 * <p>Instrumentation libraries should generally expose their own builder with controls that are
 * appropriate for that library and delegate to this class to create the {@link Instrumenter}.
 */
public final class InstrumenterBuilder<REQUEST, RESPONSE> {

  private static final SpanSuppressionStrategy spanSuppressionStrategy =
      SpanSuppressionStrategy.fromConfig(
          ConfigPropertiesUtil.getString(
              "otel.instrumentation.experimental.span-suppression-strategy"));

  final OpenTelemetry openTelemetry;
  final String instrumentationName;
  final SpanNameExtractor<? super REQUEST> spanNameExtractor;

  final List<SpanLinksExtractor<? super REQUEST>> spanLinksExtractors = new ArrayList<>();
  final List<AttributesExtractor<? super REQUEST, ? super RESPONSE>> attributesExtractors =
      new ArrayList<>();
  final List<ContextCustomizer<? super REQUEST>> contextCustomizers = new ArrayList<>();
  private final List<OperationListener> operationListeners = new ArrayList<>();
  private final List<OperationMetrics> operationMetrics = new ArrayList<>();

  @Nullable private String instrumentationVersion;
  @Nullable private String schemaUrl = null;
  SpanKindExtractor<? super REQUEST> spanKindExtractor = SpanKindExtractor.alwaysInternal();
  SpanStatusExtractor<? super REQUEST, ? super RESPONSE> spanStatusExtractor =
      SpanStatusExtractor.getDefault();
  ErrorCauseExtractor errorCauseExtractor = ErrorCauseExtractor.jdk();
  boolean enabled = true;

  InstrumenterBuilder(
      OpenTelemetry openTelemetry,
      String instrumentationName,
      SpanNameExtractor<? super REQUEST> spanNameExtractor) {
    this.openTelemetry = openTelemetry;
    this.instrumentationName = instrumentationName;
    this.spanNameExtractor = spanNameExtractor;
    this.instrumentationVersion =
        EmbeddedInstrumentationProperties.findVersion(instrumentationName);
  }

  /**
   * Sets the instrumentation version that will be associated with all telemetry produced by this
   * {@link Instrumenter}.
   *
   * @param instrumentationVersion is the version of the instrumentation library, not the version of
   *     the instrument<b>ed</b> library.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> setInstrumentationVersion(
      String instrumentationVersion) {
    this.instrumentationVersion = instrumentationVersion;
    return this;
  }

  /**
   * Sets the OpenTelemetry schema URL that will be associated with all telemetry produced by this
   * {@link Instrumenter}.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /**
   * Sets the {@link SpanStatusExtractor} that will determine the {@link StatusCode} for a response.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> setSpanStatusExtractor(
      SpanStatusExtractor<? super REQUEST, ? super RESPONSE> spanStatusExtractor) {
    this.spanStatusExtractor = spanStatusExtractor;
    return this;
  }

  /**
   * Adds a {@link AttributesExtractor} that will extract attributes from requests and responses.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> addAttributesExtractor(
      AttributesExtractor<? super REQUEST, ? super RESPONSE> attributesExtractor) {
    this.attributesExtractors.add(attributesExtractor);
    return this;
  }

  /** Adds {@link AttributesExtractor}s that will extract attributes from requests and responses. */
  public InstrumenterBuilder<REQUEST, RESPONSE> addAttributesExtractors(
      Iterable<? extends AttributesExtractor<? super REQUEST, ? super RESPONSE>>
          attributesExtractors) {
    attributesExtractors.forEach(this.attributesExtractors::add);
    return this;
  }

  /** Adds {@link AttributesExtractor}s that will extract attributes from requests and responses. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final InstrumenterBuilder<REQUEST, RESPONSE> addAttributesExtractors(
      AttributesExtractor<? super REQUEST, ? super RESPONSE>... attributesExtractors) {
    return addAttributesExtractors(Arrays.asList(attributesExtractors));
  }

  /** Adds a {@link SpanLinksExtractor} that will extract span links from requests. */
  public InstrumenterBuilder<REQUEST, RESPONSE> addSpanLinksExtractor(
      SpanLinksExtractor<REQUEST> spanLinksExtractor) {
    spanLinksExtractors.add(spanLinksExtractor);
    return this;
  }

  /**
   * Adds a {@link ContextCustomizer} that will customize the context during {@link
   * Instrumenter#start(Context, Object)}.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> addContextCustomizer(
      ContextCustomizer<? super REQUEST> contextCustomizer) {
    contextCustomizers.add(contextCustomizer);
    return this;
  }

  /**
   * Adds a {@link OperationListener} that will be called when an instrumented operation starts and
   * ends.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> addOperationListener(OperationListener listener) {
    operationListeners.add(listener);
    return this;
  }

  /**
   * Adds a {@link OperationMetrics} that will produce a {@link OperationListener} capturing the
   * requests processing metrics.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> addOperationMetrics(OperationMetrics factory) {
    operationMetrics.add(factory);
    return this;
  }

  /**
   * Sets the {@link ErrorCauseExtractor} that will extract the root cause of an error thrown during
   * request processing.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> setErrorCauseExtractor(
      ErrorCauseExtractor errorCauseExtractor) {
    this.errorCauseExtractor = errorCauseExtractor;
    return this;
  }

  /**
   * Allows enabling/disabling the {@link Instrumenter} based on the {@code enabled} value passed as
   * parameter. All instrumenters are enabled by default.
   */
  public InstrumenterBuilder<REQUEST, RESPONSE> setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#CLIENT client} spans
   * and inject context into requests.
   *
   * @deprecated Use {@link #buildClientInstrumenter(TextMapSetter)} instead.
   */
  @Deprecated
  public Instrumenter<REQUEST, RESPONSE> newClientInstrumenter(TextMapSetter<REQUEST> setter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingToDownstream(setter), SpanKindExtractor.alwaysClient());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#SERVER server} spans
   * and extract context from requests.
   *
   * @deprecated Use {@link #buildServerInstrumenter(TextMapGetter)} instead.
   */
  @Deprecated
  public Instrumenter<REQUEST, RESPONSE> newServerInstrumenter(TextMapGetter<REQUEST> getter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingFromUpstream(getter), SpanKindExtractor.alwaysServer());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#PRODUCER producer}
   * spans and inject context into requests.
   *
   * @deprecated Use {@link #buildProducerInstrumenter(TextMapSetter)} instead.
   */
  @Deprecated
  public Instrumenter<REQUEST, RESPONSE> newProducerInstrumenter(TextMapSetter<REQUEST> setter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingToDownstream(setter),
        SpanKindExtractor.alwaysProducer());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#CONSUMER consumer}
   * spans and extract context from requests.
   *
   * @deprecated Use {@link #buildConsumerInstrumenter(TextMapGetter)} instead.
   */
  @Deprecated
  public Instrumenter<REQUEST, RESPONSE> newConsumerInstrumenter(TextMapGetter<REQUEST> getter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingFromUpstream(getter),
        SpanKindExtractor.alwaysConsumer());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#INTERNAL internal}
   * spans and do no context propagation.
   *
   * @deprecated Use {@link #buildInstrumenter()} instead.
   */
  @Deprecated
  public Instrumenter<REQUEST, RESPONSE> newInstrumenter() {
    return buildInstrumenter(
        InstrumenterConstructor.internal(), SpanKindExtractor.alwaysInternal());
  }

  /**
   * Returns a new {@link Instrumenter} which will create spans with kind determined by the passed
   * {@link SpanKindExtractor} and do no context propagation.
   *
   * @deprecated Use {@link #buildInstrumenter(SpanKindExtractor)} instead.
   */
  @Deprecated
  public Instrumenter<REQUEST, RESPONSE> newInstrumenter(
      SpanKindExtractor<? super REQUEST> spanKindExtractor) {
    return buildInstrumenter(InstrumenterConstructor.internal(), spanKindExtractor);
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#CLIENT client} spans
   * and inject context into requests.
   */
  public Instrumenter<REQUEST, RESPONSE> buildClientInstrumenter(TextMapSetter<REQUEST> setter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingToDownstream(setter), SpanKindExtractor.alwaysClient());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#SERVER server} spans
   * and extract context from requests.
   */
  public Instrumenter<REQUEST, RESPONSE> buildServerInstrumenter(TextMapGetter<REQUEST> getter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingFromUpstream(getter), SpanKindExtractor.alwaysServer());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#PRODUCER producer}
   * spans and inject context into requests.
   */
  public Instrumenter<REQUEST, RESPONSE> buildProducerInstrumenter(TextMapSetter<REQUEST> setter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingToDownstream(setter),
        SpanKindExtractor.alwaysProducer());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#CONSUMER consumer}
   * spans and extract context from requests.
   */
  public Instrumenter<REQUEST, RESPONSE> buildConsumerInstrumenter(TextMapGetter<REQUEST> getter) {
    return buildInstrumenter(
        InstrumenterConstructor.propagatingFromUpstream(getter),
        SpanKindExtractor.alwaysConsumer());
  }

  /**
   * Returns a new {@link Instrumenter} which will create {@linkplain SpanKind#INTERNAL internal}
   * spans and do no context propagation.
   */
  public Instrumenter<REQUEST, RESPONSE> buildInstrumenter() {
    return buildInstrumenter(
        InstrumenterConstructor.internal(), SpanKindExtractor.alwaysInternal());
  }

  /**
   * Returns a new {@link Instrumenter} which will create spans with kind determined by the passed
   * {@link SpanKindExtractor} and do no context propagation.
   */
  public Instrumenter<REQUEST, RESPONSE> buildInstrumenter(
      SpanKindExtractor<? super REQUEST> spanKindExtractor) {
    return buildInstrumenter(InstrumenterConstructor.internal(), spanKindExtractor);
  }

  private Instrumenter<REQUEST, RESPONSE> buildInstrumenter(
      InstrumenterConstructor<REQUEST, RESPONSE> constructor,
      SpanKindExtractor<? super REQUEST> spanKindExtractor) {
    this.spanKindExtractor = spanKindExtractor;
    return constructor.create(this);
  }

  Tracer buildTracer() {
    TracerBuilder tracerBuilder =
        openTelemetry.getTracerProvider().tracerBuilder(instrumentationName);
    if (instrumentationVersion != null) {
      tracerBuilder.setInstrumentationVersion(instrumentationVersion);
    }
    if (schemaUrl != null) {
      tracerBuilder.setSchemaUrl(schemaUrl);
    }
    return tracerBuilder.build();
  }

  List<OperationListener> buildOperationListeners() {
    // just copy the listeners list if there are no metrics registered
    if (operationMetrics.isEmpty()) {
      return new ArrayList<>(operationListeners);
    }

    List<OperationListener> listeners =
        new ArrayList<>(operationListeners.size() + operationMetrics.size());
    listeners.addAll(operationListeners);

    MeterBuilder meterBuilder = openTelemetry.getMeterProvider().meterBuilder(instrumentationName);
    if (instrumentationVersion != null) {
      meterBuilder.setInstrumentationVersion(instrumentationVersion);
    }
    if (schemaUrl != null) {
      meterBuilder.setSchemaUrl(schemaUrl);
    }
    Meter meter = meterBuilder.build();
    for (OperationMetrics factory : operationMetrics) {
      listeners.add(factory.create(meter));
    }

    return listeners;
  }

  SpanSuppressor buildSpanSuppressor() {
    return spanSuppressionStrategy.create(getSpanKeysFromAttributesExtractors());
  }

  private Set<SpanKey> getSpanKeysFromAttributesExtractors() {
    return attributesExtractors.stream()
        .filter(SpanKeyProvider.class::isInstance)
        .map(SpanKeyProvider.class::cast)
        .flatMap(
            provider -> {
              SpanKey spanKey = provider.internalGetSpanKey();
              return spanKey == null ? Stream.of() : Stream.of(spanKey);
            })
        .collect(Collectors.toSet());
  }

  private interface InstrumenterConstructor<RQ, RS> {
    Instrumenter<RQ, RS> create(InstrumenterBuilder<RQ, RS> builder);

    static <RQ, RS> InstrumenterConstructor<RQ, RS> internal() {
      return Instrumenter::new;
    }

    static <RQ, RS> InstrumenterConstructor<RQ, RS> propagatingToDownstream(
        TextMapSetter<RQ> setter) {
      return builder -> new ClientInstrumenter<>(builder, setter);
    }

    static <RQ, RS> InstrumenterConstructor<RQ, RS> propagatingFromUpstream(
        TextMapGetter<RQ> getter) {
      return builder -> new ServerInstrumenter<>(builder, getter);
    }
  }
}
