/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.autoconfigure.exporters.logging;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Spring Boot auto configuration test for {@link LoggingSpanExporter}. */
class LoggingSpanExporterAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  OpenTelemetryAutoConfiguration.class,
                  LoggingSpanExporterAutoConfiguration.class));

  @Test
  @DisplayName("when exporters are ENABLED should initialize LoggingSpanExporter bean")
  void loggingEnabled() {
    this.contextRunner
        .withPropertyValues("otel.exporter.logging.enabled=true")
        .run(
            context ->
                assertThat(context.getBean("otelLoggingSpanExporter", LoggingSpanExporter.class))
                    .isNotNull());
  }

  @Test
  void loggingTracesEnabled() {
    this.contextRunner
        .withPropertyValues("otel.exporter.logging.traces.enabled=true")
        .run(
            context ->
                assertThat(context.getBean("otelLoggingSpanExporter", LoggingSpanExporter.class))
                    .isNotNull());
  }

  @Test
  @DisplayName("when exporters are DISABLED should NOT initialize LoggingSpanExporter bean")
  void loggingDisabled() {
    this.contextRunner
        .withPropertyValues("otel.exporter.logging.enabled=false")
        .run(context -> assertThat(context.containsBean("otelLoggingSpanExporter")).isFalse());
  }

  @Test
  @DisplayName("when exporters are DISABLED should NOT initialize LoggingSpanExporter bean")
  void loggingTracesDisabled() {
    this.contextRunner
        .withPropertyValues("otel.exporter.logging.traces.enabled=false")
        .run(context -> assertThat(context.containsBean("otelLoggingSpanExporter")).isFalse());
  }

  @Test
  @DisplayName(
      "when exporter enabled property is MISSING should initialize LoggingSpanExporter bean")
  void exporterPresentByDefault() {
    this.contextRunner.run(
        context ->
            assertThat(context.getBean("otelLoggingSpanExporter", LoggingSpanExporter.class))
                .isNotNull());
  }
}
