/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.instrumentation.api.util.VirtualField;
import javax.servlet.ServletOutputStream;

public class Injection {

  public static void initializeInjectionObjectIfNeeded(
      ServletOutputStream servletOutputStream,
      String characterEncoding,
      SnippetInjectingResponseWrapper wrapper) {

    InjectionState state =
        VirtualField.find(ServletOutputStream.class, InjectionState.class).get(servletOutputStream);
    if (state == null || state.getWrapper() != wrapper) {
      state = new InjectionState(wrapper);
      VirtualField.find(ServletOutputStream.class, InjectionState.class)
          .set(servletOutputStream, state);
    }
  }

  public static InjectionState getInjectionObject(ServletOutputStream servletOutputStream) {
    return VirtualField.find(ServletOutputStream.class, InjectionState.class)
        .get(servletOutputStream);
  }
}
