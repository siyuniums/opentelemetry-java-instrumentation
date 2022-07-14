/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.kafkaclients;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.kafka.internal.KafkaInstrumenterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class KafkaTelemetryBuilder {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.kafka-clients-2.6";

  private final OpenTelemetry openTelemetry;
  private final List<AttributesExtractor<ProducerRecord<?, ?>, Void>> producerAttributesExtractors =
      new ArrayList<>();
  private final List<AttributesExtractor<ConsumerRecord<?, ?>, Void>> consumerAttributesExtractors =
      new ArrayList<>();
  private boolean captureExperimentalSpanAttributes = false;
  private boolean propagationEnabled = true;

  KafkaTelemetryBuilder(OpenTelemetry openTelemetry) {
    this.openTelemetry = Objects.requireNonNull(openTelemetry);
  }

  public KafkaTelemetryBuilder addProducerAttributesExtractors(
      AttributesExtractor<ProducerRecord<?, ?>, Void> extractor) {
    producerAttributesExtractors.add(extractor);
    return this;
  }

  public KafkaTelemetryBuilder addConsumerAttributesExtractors(
      AttributesExtractor<ConsumerRecord<?, ?>, Void> extractor) {
    consumerAttributesExtractors.add(extractor);
    return this;
  }

  /**
   * Sets whether experimental attributes should be set to spans. These attributes may be changed or
   * removed in the future, so only enable this if you know you do not require attributes filled by
   * this instrumentation to be stable across versions.
   */
  public KafkaTelemetryBuilder setCaptureExperimentalSpanAttributes(
      boolean captureExperimentalSpanAttributes) {
    this.captureExperimentalSpanAttributes = captureExperimentalSpanAttributes;
    return this;
  }

  /**
   * Sets whether the producer context should be propagated from the producer span to the consumer
   * span. Enabled by default.
   */
  public KafkaTelemetryBuilder setPropagationEnabled(boolean propagationEnabled) {
    this.propagationEnabled = propagationEnabled;
    return this;
  }

  public KafkaTelemetry build() {
    KafkaInstrumenterFactory instrumenterFactory =
        new KafkaInstrumenterFactory(openTelemetry, INSTRUMENTATION_NAME)
            .setCaptureExperimentalSpanAttributes(captureExperimentalSpanAttributes)
            .setPropagationEnabled(propagationEnabled);

    return new KafkaTelemetry(
        openTelemetry,
        instrumenterFactory.createProducerInstrumenter(producerAttributesExtractors),
        instrumenterFactory.createConsumerOperationInstrumenter(
            MessageOperation.RECEIVE, consumerAttributesExtractors));
  }
}
