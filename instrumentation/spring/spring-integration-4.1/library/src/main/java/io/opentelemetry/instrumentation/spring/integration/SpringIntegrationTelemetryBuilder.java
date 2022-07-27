/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.integration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import java.util.ArrayList;
import java.util.List;

/** A builder of {@link SpringIntegrationTelemetry}. */
public final class SpringIntegrationTelemetryBuilder {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.spring-integration-4.1";

  private final OpenTelemetry openTelemetry;
  private final List<AttributesExtractor<MessageWithChannel, Void>> additionalAttributeExtractors =
      new ArrayList<>();

  private boolean producerSpanEnabled = false;

  SpringIntegrationTelemetryBuilder(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  /**
   * Adds an additional {@link AttributesExtractor} to invoke to set attributes to instrumented
   * items.
   */
  public SpringIntegrationTelemetryBuilder addAttributesExtractor(
      AttributesExtractor<MessageWithChannel, Void> attributesExtractor) {
    additionalAttributeExtractors.add(attributesExtractor);
    return this;
  }

  /**
   * Sets whether additional {@link SpanKind#PRODUCER PRODUCER} span should be emitted by this
   * instrumentation.
   */
  public SpringIntegrationTelemetryBuilder setProducerSpanEnabled(boolean producerSpanEnabled) {
    this.producerSpanEnabled = producerSpanEnabled;
    return this;
  }

  private static String consumerSpanName(MessageWithChannel messageWithChannel) {
    return messageWithChannel.getChannelName() + " process";
  }

  private static String producerSpanName(MessageWithChannel messageWithChannel) {
    return messageWithChannel.getChannelName() + " send";
  }

  /**
   * Returns a new {@link SpringIntegrationTelemetry} with the settings of this {@link
   * SpringIntegrationTelemetryBuilder}.
   */
  public SpringIntegrationTelemetry build() {
    Instrumenter<MessageWithChannel, Void> consumerInstrumenter =
        Instrumenter.<MessageWithChannel, Void>builder(
                openTelemetry,
                INSTRUMENTATION_NAME,
                SpringIntegrationTelemetryBuilder::consumerSpanName)
            .addAttributesExtractors(additionalAttributeExtractors)
            .addAttributesExtractor(
                MessagingAttributesExtractor.create(
                    SpringMessagingAttributesGetter.INSTANCE, MessageOperation.PROCESS))
            .buildConsumerInstrumenter(MessageHeadersGetter.INSTANCE);

    Instrumenter<MessageWithChannel, Void> producerInstrumenter =
        Instrumenter.<MessageWithChannel, Void>builder(
                openTelemetry,
                INSTRUMENTATION_NAME,
                SpringIntegrationTelemetryBuilder::producerSpanName)
            .addAttributesExtractors(additionalAttributeExtractors)
            .addAttributesExtractor(
                MessagingAttributesExtractor.create(
                    SpringMessagingAttributesGetter.INSTANCE, MessageOperation.SEND))
            .buildInstrumenter(SpanKindExtractor.alwaysProducer());
    return new SpringIntegrationTelemetry(
        openTelemetry.getPropagators(),
        consumerInstrumenter,
        producerInstrumenter,
        producerSpanEnabled);
  }
}
