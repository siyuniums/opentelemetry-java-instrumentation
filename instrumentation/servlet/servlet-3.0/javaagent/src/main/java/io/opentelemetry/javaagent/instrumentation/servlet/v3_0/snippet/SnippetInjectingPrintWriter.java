/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.intInjection;

import io.opentelemetry.instrumentation.api.util.VirtualField;
import java.io.PrintWriter;

public class SnippetInjectingPrintWriter extends PrintWriter {
  private final String snippet;

  //  private int headTag = -1; // record how many bits go so far for <head>
  //  private String characterEncoding;
  private final InjectionObject obj = new InjectionObject();

  public SnippetInjectingPrintWriter(PrintWriter writer, String snippet, String characterEncoding) {
    super(writer);
    obj.characterEncoding = characterEncoding;
    obj.headTag = -1;
    System.out.println("writer" + writer + writer.getClass());
    VirtualField.find(PrintWriter.class, InjectionObject.class).set(writer, obj);
    System.out.println("set vf SnippetInjectingPrintWriter");
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
    intInjection((byte) b, obj);
    super.write(b);
    if (obj.inject()) {
      // begin to insert
      super.write(this.snippet);
    }
  }

  @Override
  public void write(char[] buf, int off, int len) {
    super.write(buf, off, len);
    for (int i = off; i < buf.length && i - off < len; i++) {
      char b = buf[i];
      write(b);
    }
  }
}
