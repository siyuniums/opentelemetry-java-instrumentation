/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.IOException;
import java.nio.charset.Charset;
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
    InjectionObject obj = getInjectionObject(servletOutputStream);
    System.out.println(
        "- " + new String(write, Charset.defaultCharset()).substring(off, off + len) + " enter");
    if (obj.injected()) {
      System.out.println(
          "- " + new String(write, Charset.defaultCharset()).substring(off, off + len) + " write");
      return true;
    } else {
      boolean result = obj.stringInjection(servletOutputStream, write, off, len);
      return result;
    }
  }
}
