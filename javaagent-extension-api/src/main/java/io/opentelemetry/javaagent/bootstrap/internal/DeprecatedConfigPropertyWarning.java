/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.bootstrap.internal;

import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DeprecatedConfigPropertyWarning {

  private static final Logger logger =
      Logger.getLogger(DeprecatedConfigPropertyWarning.class.getName());

  public static void warnIfUsed(
      InstrumentationConfig config, String deprecatedPropertyName, String newPropertyName) {
    if (config.getString(deprecatedPropertyName) != null) {
      logger.log(
          WARNING,
          "Deprecated property \"{0}\" was used; use the \"{1}\" property instead",
          new Object[] {deprecatedPropertyName, newPropertyName});
    }
  }

  private DeprecatedConfigPropertyWarning() {}
}
