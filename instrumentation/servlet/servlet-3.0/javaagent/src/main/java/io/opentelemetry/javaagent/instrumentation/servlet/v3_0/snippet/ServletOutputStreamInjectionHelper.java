package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

public class ServletOutputStreamInjectionHelper {

  /**
   * return true means injected already, return false means didn't inject anything
   * Servlet3OutputStreamWriteAdvice would skip the write method when get return value ture, and
   * would write the original bytes when get return value false.
   */
  public static boolean process(
      InjectionState state, ServletOutputStream sp, byte[] original, int off, int length)
      throws IOException {
    if (state.alreadyInjected() || state.characterEncoding == null) {
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
    if (shouldInject) {
      state.setAlreadyInjected(); // set before write to avoid recursive loop
      sp.write(original, off, i + 1);
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(state.characterEncoding);
      sp.write(snippetBytes);
      sp.write(original, i + 1, length - i - 1);
      return true;
    } else {
      return false;
    }
  }

  public static boolean process(InjectionState state, ServletOutputStream sp, byte b)
      throws IOException {
    if (state.alreadyInjected() || state.characterEncoding == null) {
      return false;
    }
    if (state.processByte(b)) {
      state.setAlreadyInjected(); // set before write to avoid recursive loop
      sp.write(b);
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(state.characterEncoding);
      sp.write(snippetBytes);
      return true;
    } else {
      return false;
    }
  }
}
