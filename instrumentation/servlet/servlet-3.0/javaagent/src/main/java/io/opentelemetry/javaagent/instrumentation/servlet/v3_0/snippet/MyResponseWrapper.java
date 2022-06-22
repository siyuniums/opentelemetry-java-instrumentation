/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class MyResponseWrapper extends HttpServletResponseWrapper {

  private OutputStream output;
  private static final int SNIPPET_LENGTH = 79;

  public MyResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public void setHeader(String name, String value) {
    if ("Content-Length".equals(name)) {
      value = Integer.toString(SNIPPET_LENGTH + Integer.valueOf(value));
      System.out.println("------------- now setHeader should make content length" + value);
    }
    super.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {

    if ("Content-Length".equals(name)) {
      value = Integer.toString(SNIPPET_LENGTH + Integer.valueOf(value));
      System.out.println("------------- now addheader should make content length " + value);
    }
    super.addHeader(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {
    System.out.println("______ setIntHeader called name: " + name + " value: " + value);
    super.setIntHeader(name, value);
  }

  @Override
  public void addIntHeader(String name, int value) {
    System.out.println("______ addIntHeader called name: " + name + " value: " + value);
    super.addIntHeader(name, value);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (getContentType() != null && getContentType().contains("text/html")) {
      MyServletOutputStream myStream = new MyServletOutputStream(super.getOutputStream());
      System.out.println("----- MyServletOutputStream");
      return myStream;
    } else {
      return super.getOutputStream();
    }
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (getContentType() != null && getContentType().contains("text/html")) {
      return new MyPrintWriter(super.getWriter());
    } else {
      return super.getWriter();
    }
  }

  @Override
  public void setContentLength(int len) {
    System.out.println("----- setContentLength" + len);
    super.setContentLength(SNIPPET_LENGTH + len);
  }

  @Override
  public void setContentLengthLong(long len) {
    System.out.println("----- setContentLengthLong" + len);
    super.setContentLengthLong(len + SNIPPET_LENGTH);
  }

  // for debug use only
  @Override
  public void setContentType(String type) {
    System.out.println("now content type is set to be" + type);
    super.setContentType(type);
  }
}
