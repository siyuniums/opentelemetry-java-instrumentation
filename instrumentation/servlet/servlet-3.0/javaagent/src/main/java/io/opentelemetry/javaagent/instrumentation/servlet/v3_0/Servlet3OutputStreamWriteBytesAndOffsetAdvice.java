/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;
import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.ServletOutputStreamInjectionHelper.process;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionState;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteBytesAndOffsetAdvice {
  @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class, suppress = Throwable.class)
  public static boolean methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) byte[] write,
      @Advice.Argument(value = 1, readOnly = false) int off,
      @Advice.Argument(value = 2, readOnly = false) int len)
      throws IOException {
    InjectionState obj = getInjectionObject(servletOutputStream);
    boolean result = !process(obj, servletOutputStream, write, off, len);
    return result;
  }
}
