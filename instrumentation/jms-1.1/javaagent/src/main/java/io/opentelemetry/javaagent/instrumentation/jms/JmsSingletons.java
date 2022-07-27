/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jms;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;
import io.opentelemetry.javaagent.bootstrap.internal.ExperimentalConfig;

public final class JmsSingletons {
  private static final String INSTRUMENTATION_NAME = "io.opentelemetry.jms-1.1";

  private static final Instrumenter<MessageWithDestination, Void> PRODUCER_INSTRUMENTER =
      buildProducerInstrumenter();
  private static final Instrumenter<MessageWithDestination, Void> CONSUMER_INSTRUMENTER =
      buildConsumerInstrumenter();
  private static final Instrumenter<MessageWithDestination, Void> LISTENER_INSTRUMENTER =
      buildListenerInstrumenter();

  private static Instrumenter<MessageWithDestination, Void> buildProducerInstrumenter() {
    JmsMessageAttributesGetter getter = JmsMessageAttributesGetter.INSTANCE;
    MessageOperation operation = MessageOperation.SEND;

    return Instrumenter.<MessageWithDestination, Void>builder(
            GlobalOpenTelemetry.get(),
            INSTRUMENTATION_NAME,
            MessagingSpanNameExtractor.create(getter, operation))
        .addAttributesExtractor(MessagingAttributesExtractor.create(getter, operation))
        .buildProducerInstrumenter(MessagePropertySetter.INSTANCE);
  }

  private static Instrumenter<MessageWithDestination, Void> buildConsumerInstrumenter() {
    JmsMessageAttributesGetter getter = JmsMessageAttributesGetter.INSTANCE;
    MessageOperation operation = MessageOperation.RECEIVE;

    // MessageConsumer does not do context propagation
    return Instrumenter.<MessageWithDestination, Void>builder(
            GlobalOpenTelemetry.get(),
            INSTRUMENTATION_NAME,
            MessagingSpanNameExtractor.create(getter, operation))
        .addAttributesExtractor(MessagingAttributesExtractor.create(getter, operation))
        .setEnabled(ExperimentalConfig.get().messagingReceiveInstrumentationEnabled())
        .buildInstrumenter(SpanKindExtractor.alwaysConsumer());
  }

  private static Instrumenter<MessageWithDestination, Void> buildListenerInstrumenter() {
    JmsMessageAttributesGetter getter = JmsMessageAttributesGetter.INSTANCE;
    MessageOperation operation = MessageOperation.PROCESS;

    return Instrumenter.<MessageWithDestination, Void>builder(
            GlobalOpenTelemetry.get(),
            INSTRUMENTATION_NAME,
            MessagingSpanNameExtractor.create(getter, operation))
        .addAttributesExtractor(MessagingAttributesExtractor.create(getter, operation))
        .buildConsumerInstrumenter(MessagePropertyGetter.INSTANCE);
  }

  public static Instrumenter<MessageWithDestination, Void> producerInstrumenter() {
    return PRODUCER_INSTRUMENTER;
  }

  public static Instrumenter<MessageWithDestination, Void> consumerInstrumenter() {
    return CONSUMER_INSTRUMENTER;
  }

  public static Instrumenter<MessageWithDestination, Void> listenerInstrumenter() {
    return LISTENER_INSTRUMENTER;
  }

  private JmsSingletons() {}
}
