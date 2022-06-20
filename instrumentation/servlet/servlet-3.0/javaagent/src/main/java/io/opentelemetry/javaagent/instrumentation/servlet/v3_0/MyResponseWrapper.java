/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MyResponseWrapper extends HttpServletResponseWrapper {

  private OutputStream output;
  public MyResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  // only used by tests
  public String getResult() throws IOException {

    return this.getOutputStream().toString();
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (getContentType() != null && getContentType().contains("text/html")){
      MyServletOutputStream myStream = new MyServletOutputStream(super.getOutputStream());
      return myStream;
    } else {
      return super.getOutputStream();
    }

  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (getContentType() != null && getContentType().contains("text/html")){
      return new MyPrintWriter(super.getWriter());
    } else {
      return super.getWriter();
    }

  }

  // for debug use only
  @Override
  public void setContentType(String type) {
    System.out.println("now content type is set to be" + type);
    super.setContentType(type);
  }
  
}
