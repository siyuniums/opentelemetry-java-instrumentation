/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteBytesAndOffsetAdvice {
  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static void methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) byte[] write,
      @Advice.Argument(value = 1, readOnly = false) int off,
      @Advice.Argument(value = 2, readOnly = false) int len)
      throws UnsupportedEncodingException {
    InjectionObject obj = getInjectionObject(servletOutputStream);
    byte[] result = obj.stringInjection(write, off, len);
    if (result != null) {
      write = result;
      len = result.length;
      off = 0;
    }
  }
}
