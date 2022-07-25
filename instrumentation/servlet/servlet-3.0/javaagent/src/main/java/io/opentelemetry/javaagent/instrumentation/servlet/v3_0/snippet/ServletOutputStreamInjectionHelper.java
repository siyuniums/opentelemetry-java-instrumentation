package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionState.ALREADY_INJECTED_FAKE_VALUE;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

public class ServletOutputStreamInjectionHelper {
  public static boolean process(
      InjectionState state, ServletOutputStream sp, byte[] original, int off, int length)
      throws IOException {
    if (state.alreadyInjected() || state.characterEncoding == null) {
      return true;
    }
    int i;
    for (i = off; i < length && i - off < length; i++) {
      if (state.processByte(original[i])) {
        break;
      }
    }
    if (state.shouldInject()) {
      state.headTagBytesSeen = ALREADY_INJECTED_FAKE_VALUE; // set before write to avoid loop
      sp.write(original, off, i + 1);
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(state.characterEncoding);
      sp.write(snippetBytes);
      sp.write(original, i + 1, length - i - 1);
      return false;
    } else {
      return true;
    }
  }

  public static boolean process(InjectionState state, ServletOutputStream sp, byte b)
      throws IOException {
    if (state.alreadyInjected() || state.characterEncoding == null) {
      return true;
    }
    if (state.processByte(b)) {
      state.headTagBytesSeen = ALREADY_INJECTED_FAKE_VALUE; // set before write to avoid loop
      sp.write(b);
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(state.characterEncoding);
      sp.write(snippetBytes);
      return false;
    } else {
      return true;
    }
  }
}
