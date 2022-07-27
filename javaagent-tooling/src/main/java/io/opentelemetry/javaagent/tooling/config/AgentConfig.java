/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.tooling.config;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

public final class AgentConfig {

  public static boolean isInstrumentationEnabled(
      ConfigProperties config, Iterable<String> instrumentationNames, boolean defaultEnabled) {
    // If default is enabled, we want to enable individually,
    // if default is disabled, we want to disable individually.
    boolean anyEnabled = defaultEnabled;
    for (String name : instrumentationNames) {
      String propertyName = "otel.instrumentation." + name + ".enabled";
      boolean enabled = config.getBoolean(propertyName, defaultEnabled);

      if (defaultEnabled) {
        anyEnabled &= enabled;
      } else {
        anyEnabled |= enabled;
      }
    }
    return anyEnabled;
  }

  public static boolean isDebugModeEnabled(ConfigProperties config) {
    return config.getBoolean("otel.javaagent.debug", false);
  }

  private AgentConfig() {}
}
