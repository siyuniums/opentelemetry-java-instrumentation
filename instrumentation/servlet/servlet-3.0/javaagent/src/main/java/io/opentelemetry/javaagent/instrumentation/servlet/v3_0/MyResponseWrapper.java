/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class MyResponseWrapper extends HttpServletResponseWrapper {

    private OutputStream output;
    public MyResponseWrapper(HttpServletResponse response) {
        super(response);
    }

  @Override
  public void setIntHeader(String name, int value) {
    System.out.println("______ setIntHeader called");
    super.setIntHeader(name, value);
  }


  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    System.out.println("______ getOutputStream called");

    return super.getOutputStream();
  }

  @Override
    public PrintWriter getWriter() throws IOException {
    System.out.println("______ getWriter called");
    return super.getWriter();
    }

  @Override
  public void setContentLength(int len) {
    System.out.println("setContentLength" + len);
    super.setContentLength(len);
  }

  // for debug use only
    @Override
    public void setContentType(String type) {
        System.out.println("setContentType " + type);
        super.setContentType(type);
    }

  @Override
  public void setBufferSize(int size) {
    System.out.println("setBufferSize" + size);
    super.setBufferSize(size);
  }


}
