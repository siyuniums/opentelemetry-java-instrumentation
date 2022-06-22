/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class MyServletOutputStream extends ServletOutputStream {
  ServletOutputStream sp;
  int headTag = -1; // record how many bits go so far for <head>

  private static final String SNIPPET =
      "<script type=\"text/javascript\">\n"
          + " function msg(){ alert(\"TEST HERE\");}\n"
          + "</script>";

  public MyServletOutputStream(ServletOutputStream outputStream) {
    this.sp = outputStream;
  }

  @Override
  public void write(int b) throws IOException {
    if (headTag == -1 && b == 60) { // ASCII for <
      headTag = 0;
    } else if (headTag == 0 && b == 104) { // ASCII for h
      headTag = 1;
    } else if (headTag == 1 && b == 101) { // ASCII for e
      headTag = 2;
    } else if (headTag == 2 && b == 97) { // ASCII for a
      headTag = 3;
    } else if (headTag == 3 && b == 100) { // ASCII for d
      headTag = 4;
    } else if (headTag == 4 && b == 62) { // ASCII for >
      headTag = 5;
    } else {
      headTag = -1;
    }
    sp.write(b);
    if (headTag == 5) {
      // begin to insert
      for (int i = 0; i < SNIPPET.length(); i++) {
        sp.write((int) SNIPPET.charAt(i));
      }
      System.out.println("--snippet length" + SNIPPET.length());
    }
  }

  @Override
  public boolean isReady() {
    return sp.isReady();
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
    sp.setWriteListener(writeListener);
  }
}
