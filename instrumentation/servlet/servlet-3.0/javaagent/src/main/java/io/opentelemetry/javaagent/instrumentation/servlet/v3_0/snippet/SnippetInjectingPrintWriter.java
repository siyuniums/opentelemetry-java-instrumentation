/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.PrintWriter;

public class SnippetInjectingPrintWriter extends PrintWriter {
  private final String snippet;
  private final InjectionObject obj = new InjectionObject();
  private final SnippetInjectingResponseWrapper wrapper;

  public SnippetInjectingPrintWriter(
      PrintWriter writer,
      String snippet,
      String characterEncoding,
      SnippetInjectingResponseWrapper wrapper) {
    super(writer);
    obj.characterEncoding = characterEncoding;
    obj.headTagBytesSeen = -1;
    this.snippet = snippet;
    this.wrapper = wrapper;
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
    obj.intInjectionHelper((byte) b);
    super.write(b);
    if (obj.inject()) {
      // begin to insert
      System.out.println("printWrtier begin to inject");
      obj.headTagBytesSeen = -2;
      super.write(this.snippet);
      if (wrapper.contentLength > 0) {
        wrapper.setHeader(
            "Content-Length",
            Long.toString(wrapper.contentLength + SnippetHolder.getSnippet().length()));
      } else {
        wrapper.addLength = true;
      }
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
