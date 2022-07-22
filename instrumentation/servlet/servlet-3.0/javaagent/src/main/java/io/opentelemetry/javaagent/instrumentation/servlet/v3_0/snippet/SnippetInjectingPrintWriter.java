/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import java.io.PrintWriter;

public class SnippetInjectingPrintWriter extends PrintWriter {
  private final String snippet;
  private final InjectionObject obj = new InjectionObject();

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
      //      if (obj.injected()) {
      //        System.out.println(" ");
      //      }
      write(b);
    }
  }

  @Override
  public void write(int b) {
    obj.intInjectionHelper((byte) b);
    super.write(b);
    System.out.println("-- " + (char) b);
    if (obj.inject()) {
      // begin to insert
      obj.headTagBytesSeen = -2;
      System.out.println("after reset" + obj.inject());
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
