/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteIntAdvice {

  @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class, suppress = Throwable.class)
  public static boolean methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) int write)
      throws IOException {
    InjectionObject injectObj = getInjectionObject(servletOutputStream);
    if (injectObj.injected()) {
      return true;
    } else {
      boolean injected = injectObj.intInjection(servletOutputStream, (byte) write);
      return injected;
    }
  }
}
