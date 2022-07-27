/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.log4j.appender.v2_17.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.appender.internal.LogBuilder;
import io.opentelemetry.instrumentation.api.appender.internal.Severity;
import io.opentelemetry.instrumentation.api.internal.cache.Cache;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class LogEventMapper<T> {

  private static final String SPECIAL_MAP_MESSAGE_ATTRIBUTE = "message";

  private static final Cache<String, AttributeKey<String>> contextDataAttributeKeyCache =
      Cache.bounded(100);
  private static final Cache<String, AttributeKey<String>> mapMessageAttributeKeyCache =
      Cache.bounded(100);

  private final ContextDataAccessor<T> contextDataAccessor;

  private final boolean captureExperimentalAttributes;
  private final boolean captureMapMessageAttributes;
  private final List<String> captureContextDataAttributes;
  private final boolean captureAllContextDataAttributes;

  public LogEventMapper(
      ContextDataAccessor<T> contextDataAccessor,
      boolean captureExperimentalAttributes,
      boolean captureMapMessageAttributes,
      List<String> captureContextDataAttributes) {

    this.contextDataAccessor = contextDataAccessor;
    this.captureExperimentalAttributes = captureExperimentalAttributes;
    this.captureMapMessageAttributes = captureMapMessageAttributes;
    this.captureContextDataAttributes = captureContextDataAttributes;
    this.captureAllContextDataAttributes =
        captureContextDataAttributes.size() == 1 && captureContextDataAttributes.get(0).equals("*");
  }

  /**
   * Map the {@link LogEvent} data model onto the {@link LogBuilder}. Unmapped fields include:
   *
   * <ul>
   *   <li>Fully qualified class name - {@link LogEvent#getLoggerFqcn()}
   *   <li>Thread name - {@link LogEvent#getThreadName()}
   *   <li>Thread id - {@link LogEvent#getThreadId()}
   *   <li>Thread priority - {@link LogEvent#getThreadPriority()}
   *   <li>Marker - {@link LogEvent#getMarker()}
   *   <li>Nested diagnostic context - {@link LogEvent#getContextStack()}
   * </ul>
   */
  public void mapLogEvent(
      LogBuilder builder,
      Message message,
      Level level,
      @Nullable Throwable throwable,
      T contextData) {

    AttributesBuilder attributes = Attributes.builder();

    captureMessage(builder, attributes, message);

    if (level != null) {
      builder.setSeverity(levelToSeverity(level));
      builder.setSeverityText(level.name());
    }

    if (throwable != null) {
      setThrowable(attributes, throwable);
    }

    captureContextDataAttributes(attributes, contextData);

    if (captureExperimentalAttributes) {
      Thread currentThread = Thread.currentThread();
      attributes.put(SemanticAttributes.THREAD_NAME, currentThread.getName());
      attributes.put(SemanticAttributes.THREAD_ID, currentThread.getId());
    }

    builder.setAttributes(attributes.build());

    builder.setContext(Context.current());
  }

  // visible for testing
  void captureMessage(LogBuilder builder, AttributesBuilder attributes, Message message) {
    if (message == null) {
      return;
    }
    if (!(message instanceof MapMessage)) {
      builder.setBody(message.getFormattedMessage());
      return;
    }

    MapMessage<?, ?> mapMessage = (MapMessage<?, ?>) message;

    String body = mapMessage.getFormat();
    boolean checkSpecialMapMessageAttribute = (body == null || body.isEmpty());
    if (checkSpecialMapMessageAttribute) {
      body = mapMessage.get(SPECIAL_MAP_MESSAGE_ATTRIBUTE);
    }

    if (body != null && !body.isEmpty()) {
      builder.setBody(body);
    }

    if (captureMapMessageAttributes) {
      // TODO (trask) this could be optimized in 2.9 and later by calling MapMessage.forEach()
      mapMessage
          .getData()
          .forEach(
              (key, value) -> {
                if (value != null
                    && (!checkSpecialMapMessageAttribute
                        || !key.equals(SPECIAL_MAP_MESSAGE_ATTRIBUTE))) {
                  attributes.put(
                      mapMessageAttributeKeyCache.computeIfAbsent(key, AttributeKey::stringKey),
                      value.toString());
                }
              });
    }
  }

  // visible for testing
  void captureContextDataAttributes(AttributesBuilder attributes, T contextData) {

    if (captureAllContextDataAttributes) {
      contextDataAccessor.forEach(
          contextData,
          (key, value) -> {
            if (value != null) {
              attributes.put(getContextDataAttributeKey(key), value.toString());
            }
          });
      return;
    }

    for (String key : captureContextDataAttributes) {
      Object value = contextDataAccessor.getValue(contextData, key);
      if (value != null) {
        attributes.put(getContextDataAttributeKey(key), value.toString());
      }
    }
  }

  public static AttributeKey<String> getContextDataAttributeKey(String key) {
    return contextDataAttributeKeyCache.computeIfAbsent(
        key, k -> AttributeKey.stringKey("log4j.context_data." + k));
  }

  private static void setThrowable(AttributesBuilder attributes, Throwable throwable) {
    // TODO (trask) extract method for recording exception into
    // instrumentation-appender-api-internal
    attributes.put(SemanticAttributes.EXCEPTION_TYPE, throwable.getClass().getName());
    attributes.put(SemanticAttributes.EXCEPTION_MESSAGE, throwable.getMessage());
    StringWriter writer = new StringWriter();
    throwable.printStackTrace(new PrintWriter(writer));
    attributes.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());
  }

  private static Severity levelToSeverity(Level level) {
    switch (level.getStandardLevel()) {
      case ALL:
      case TRACE:
        return Severity.TRACE;
      case DEBUG:
        return Severity.DEBUG;
      case INFO:
        return Severity.INFO;
      case WARN:
        return Severity.WARN;
      case ERROR:
        return Severity.ERROR;
      case FATAL:
        return Severity.FATAL;
      case OFF:
        return Severity.UNDEFINED_SEVERITY_NUMBER;
    }
    return Severity.UNDEFINED_SEVERITY_NUMBER;
  }
}
