/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import java.io.PrintWriter;

public class SnippetInjectingPrintWriter extends PrintWriter {
  private final String snippet;
  private InjectionState state = null;

  public SnippetInjectingPrintWriter(PrintWriter writer, String snippet, String characterEncoding) {
    super(writer);
    state = new InjectionState(characterEncoding);
    this.snippet = snippet;
  }

  @Override
  public void write(String s, int off, int len) {
    for (int i = off; i < s.length() && i - off < len; i++) {
      char b = s.charAt(i);
      write(b);
    }
  }

  @Override
  public void write(int b) {
    boolean shouldInject = state.processByte((byte) b);
    super.write(b);
    if (shouldInject) {
      // begin to insert
      state.setAlreadyInjected(); // set before write to avoid recursive loop
      super.write(this.snippet);
    }
  }

  @Override
  public void write(char[] buf, int off, int len) {
    for (int i = off; i < buf.length && i - off < len; i++) {
      char b = buf[i];
      write(b);
    }
  }
}
