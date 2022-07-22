/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteBytesAdvice {

  @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class, suppress = Throwable.class)
  public static boolean methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) byte[] write)
      throws IOException {
    InjectionObject obj = getInjectionObject(servletOutputStream);
    System.out.println("BytesAdvice- " + new String(write, Charset.defaultCharset()) + " enter");
    if (obj.injected()) {
      System.out.println("BytesAdvice- " + new String(write, Charset.defaultCharset()) + " write");
      return true;
    } else {
      boolean result = obj.stringInjection(servletOutputStream, write, 0, write.length);
      return result;
    }
  }
}
