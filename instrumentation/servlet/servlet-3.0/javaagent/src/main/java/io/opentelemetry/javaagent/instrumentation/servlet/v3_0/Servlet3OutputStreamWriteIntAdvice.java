/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionObject;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import net.bytebuddy.asm.Advice;

public class Servlet3OutputStreamWriteIntAdvice {

  @Advice.OnMethodEnter(suppress = Throwable.class)
  public static void methodEnter(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Argument(value = 0, readOnly = false) int write,
      @Advice.Local("injectObj") InjectionObject injectObj) {
    injectObj = getInjectionObject(servletOutputStream);
    injectObj.intInjection((byte) write);
  }

  @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
  public static void inject(
      @Advice.This ServletOutputStream servletOutputStream,
      @Advice.Local("injectObj") InjectionObject injectObj)
      throws IOException {
    if (injectObj.inject()) {
      // inject happen here
      servletOutputStream.print(SnippetHolder.getSnippet());
    }
  }
}
