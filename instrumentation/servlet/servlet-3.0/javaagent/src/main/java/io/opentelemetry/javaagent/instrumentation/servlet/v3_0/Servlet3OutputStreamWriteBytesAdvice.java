/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;
import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.stringInjection;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectedInfo;
import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteBytesAdvice {

  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static void methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) byte[] write)
      throws UnsupportedEncodingException {
    InjectionObject obj = getInjectionObject(servletOutputStream);
    InjectedInfo info = stringInjection(write, 0, write.length, obj);

    write = info.bits;

    System.out.println("inside bytesadvice" + new String(write, Charset.defaultCharset()));
  }
}
