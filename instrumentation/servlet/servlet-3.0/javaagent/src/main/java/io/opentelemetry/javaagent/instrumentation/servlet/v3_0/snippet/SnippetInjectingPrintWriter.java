/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import java.io.PrintWriter;

public class SnippetInjectingPrintWriter extends PrintWriter {
  private final String snippet;
  private final InjectionState state;

  public SnippetInjectingPrintWriter(
      PrintWriter writer, String snippet, SnippetInjectingResponseWrapper wrapper) {
    super(writer);
    state = new InjectionState(wrapper);
    this.snippet = snippet;
  }

  @Override
  public void write(String s, int off, int len) {
    for (int i = off; i < s.length() && i - off < len; i++) {
      write(s.charAt(i));
    }
  }

  @Override
  public void write(int b) {
    boolean shouldInject = state.processByte(b);
    super.write(b);
    if (shouldInject) {
      // set before write to avoid recursive loop since super.write(String) may delegate back to
      // write(int)
      state.setAlreadyInjected();
      long originalContentLen = state.getWrapper().contentLength;
      if (originalContentLen != 0 && state.getWrapper().isCommitted()) {
        // header already set and sent
        return;
      }
      super.write(snippet);
      state.getWrapper().injected = true;
      if (originalContentLen != 0) {
        // ContentLen already set, need to update it.
        state.getWrapper().setContentLength((int) state.getWrapper().contentLength);
      }
    }
  }

  @Override
  public void write(char[] buf, int off, int len) {
    for (int i = off; i < buf.length && i - off < len; i++) {
      write(buf[i]);
    }
  }
}
