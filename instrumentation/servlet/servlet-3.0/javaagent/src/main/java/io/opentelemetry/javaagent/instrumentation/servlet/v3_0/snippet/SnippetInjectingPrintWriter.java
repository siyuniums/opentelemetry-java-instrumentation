/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.InjectionState.ALREADY_INJECTED_FAKE_VALUE;

import java.io.PrintWriter;

public class SnippetInjectingPrintWriter extends PrintWriter {
  private final String snippet;
  private final InjectionState obj = new InjectionState();

  public SnippetInjectingPrintWriter(PrintWriter writer, String snippet, String characterEncoding) {
    super(writer);
    obj.characterEncoding = characterEncoding;
    obj.headTagBytesSeen = -1;
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
    boolean shouldInject = obj.processByte((byte) b);
    super.write(b);
    if (shouldInject) {
      // begin to insert
      obj.headTagBytesSeen = ALREADY_INJECTED_FAKE_VALUE;
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
