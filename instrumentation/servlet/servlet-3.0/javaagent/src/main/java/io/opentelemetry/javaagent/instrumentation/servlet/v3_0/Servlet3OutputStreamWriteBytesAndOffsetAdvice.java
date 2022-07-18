/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectedInfo;
import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteBytesAndOffsetAdvice {
  @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class, suppress = Throwable.class)
  public static boolean methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) byte[] write,
      @Advice.Argument(1) int off,
      @Advice.Argument(value = 2, readOnly = false) int len)
      throws UnsupportedEncodingException {
    InjectionObject obj = getInjectionObject(servletOutputStream);
    InjectedInfo info = obj.stringInjection(write, off, len);
    if (info != null) {
      write = info.bytes;
      len = info.length;
    }
    return false;
  }
}
