package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.instrumentation.api.util.VirtualField;
import javax.servlet.ServletOutputStream;

public class Injection {

  public static InjectionObject getInjectionObject(ServletOutputStream servletOutputStream) {

    InjectionObject virtualObj =
        VirtualField.find(ServletOutputStream.class, InjectionObject.class)
            .get(servletOutputStream);
    if (virtualObj == null) {
      virtualObj = new InjectionObject();
      setInjectionObject(servletOutputStream, virtualObj);
    }
    return virtualObj;
  }

  private static void setInjectionObject(
      ServletOutputStream servletOutputStream, InjectionObject obj) {
    VirtualField.find(ServletOutputStream.class, InjectionObject.class)
        .set(servletOutputStream, obj);
  }
}
