/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;


import java.io.PrintWriter;

public class MyPrintWriter extends PrintWriter {

  private static final String SNIPPET =
      "<script type=\"text/javascript\">\n"
          + " function msg(){ alert(\"TEST TEST\");}\n"
          + "</script>";


  public MyPrintWriter(PrintWriter writer) {
    super(writer);
  }

  @Override
  public void write(String str) {
    int index = str.indexOf("<head>");
    if (index == -1) {
      super.write(str);
      return;
    }

    super.write(str.substring(0, index + 6));
    super.write(SNIPPET);
    super.write(str.substring(index + 6));
  }
}