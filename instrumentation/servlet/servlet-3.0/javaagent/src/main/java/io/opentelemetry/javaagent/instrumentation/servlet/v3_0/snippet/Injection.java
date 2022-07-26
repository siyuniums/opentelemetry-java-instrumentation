package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.instrumentation.api.util.VirtualField;
import javax.servlet.ServletOutputStream;

public class Injection {

  public static InjectionState getInjectionObject(ServletOutputStream servletOutputStream) {

    InjectionState state =
        VirtualField.find(ServletOutputStream.class, InjectionState.class).get(servletOutputStream);
    if (state == null) {
      state = new InjectionState();
      VirtualField.find(ServletOutputStream.class, InjectionState.class)
          .set(servletOutputStream, state);
    }
    return state;
  }
}
