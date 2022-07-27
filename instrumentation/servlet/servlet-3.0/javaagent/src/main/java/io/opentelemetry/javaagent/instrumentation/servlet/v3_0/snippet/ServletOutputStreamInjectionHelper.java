package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletOutputStream;

public class ServletOutputStreamInjectionHelper {

  /**
   * return true means this method performed the injection, return false means it didn't inject
   * anything Servlet3OutputStreamWriteAdvice would skip the write method when the return value is
   * true, and would write the original bytes when the return value is false.
   */
  public static boolean handleWrite(
      byte[] original, int off, int length, InjectionState state, ServletOutputStream out)
      throws IOException {
    if (state.isAlreadyInjected() || state.getCharacterEncoding() == null) {
      return false;
    }
    int i;
    boolean shouldInject = false;
    for (i = off; i < length && i - off < length; i++) {
      if (state.processByte(original[i])) {
        shouldInject = true;
        break;
      }
    }
    if (!shouldInject) {
      return false;
    }
    state.setAlreadyInjected(); // set before write to avoid recursive loop
    out.write(original, off, i + 1);
    try {
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(state.getCharacterEncoding());
      out.write(snippetBytes);
    } catch (UnsupportedEncodingException ignore) {
      System.out.println(ignore);
    }
    out.write(original, i + 1, length - i - 1);
    return true;
  }

  public static boolean handleWrite(InjectionState state, ServletOutputStream sp, byte b)
      throws IOException {
    if (state.isAlreadyInjected() || state.getCharacterEncoding() == null) {
      return false;
    }
    if (!state.processByte(b)) {
      return false;
    }
    state.setAlreadyInjected(); // set before write to avoid recursive loop
    sp.write(b);
    try {
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(state.getCharacterEncoding());
      sp.write(snippetBytes);
    } catch (UnsupportedEncodingException ignore) {
      System.out.println(ignore);
    }
    return true;
  }
}
