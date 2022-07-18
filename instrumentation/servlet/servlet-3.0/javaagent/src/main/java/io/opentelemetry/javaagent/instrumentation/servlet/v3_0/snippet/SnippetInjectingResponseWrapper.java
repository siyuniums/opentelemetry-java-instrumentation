/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getInjectionObject;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SnippetInjectingResponseWrapper extends HttpServletResponseWrapper {
  private static final String SNIPPET = SnippetHolder.getSnippet();
  private static final int SNIPPET_LENGTH = SNIPPET.length();
  public static final String FAKE_SNIPPET_HEADER = "FAKE_SNIPPET_HEADER";

  public SnippetInjectingResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public boolean containsHeader(String name) {
    // override this function in order to make sure the response is wrapped
    // but not wrapped twice
    // we didn't use the traditional method req.setattribute because
    // async may carry out the old req and new resp without wrapped
    if (name.equals(FAKE_SNIPPET_HEADER)) {
      return true;
    }
    return super.containsHeader(name);
  }

  @Override
  public void setHeader(String name, String value) {
    String contentType = super.getContentType();
    if (contentType != null
        && contentType.contains("text/html")
        && "Content-Length".equalsIgnoreCase(name)) {
      try {
        value = Integer.toString(SNIPPET_LENGTH + Integer.valueOf(value));
      } catch (NumberFormatException ex) {
        System.err.println("Invalid string format");
      }
    }
    super.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {

    String contentType = super.getContentType();
    if (contentType != null
        && contentType.contains("text/html")
        && "Content-Length".equalsIgnoreCase(name)) {
      try {
        value = Integer.toString(SNIPPET_LENGTH + Integer.valueOf(value));
      } catch (NumberFormatException ex) {
        System.err.println("Invalid string format");
      }
    }
    super.addHeader(name, value);
  }

  @Override
  public void setIntHeader(String name, int value) {

    String contentType = super.getContentType();
    if (contentType != null
        && contentType.contains("text/html")
        && "Content-Length".equalsIgnoreCase(name)) {
      value = SNIPPET_LENGTH + value;
    }
    super.setIntHeader(name, value);
  }

  @Override
  public void addIntHeader(String name, int value) {
    String contentType = super.getContentType();
    if (contentType != null
        && contentType.contains("text/html")
        && "Content-Length".equalsIgnoreCase(name)) {
      value = SNIPPET_LENGTH + value;
    }
    super.addIntHeader(name, value);
  }

  @Override
  public void setContentLength(int len) {
    String contentType = super.getContentType();
    if (contentType != null && contentType.contains("text/html")) {
      len = len + SNIPPET_LENGTH;
    }
    super.setContentLength(len);
  }

  // TODO: support setContentLengthLong

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    ServletOutputStream output = super.getOutputStream();
    InjectionObject obj = getInjectionObject(output);
    obj.characterEncoding = getCharacterEncoding();
    return output;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    String contentType = super.getContentType();
    if (contentType != null && contentType.contains("text/html")) {
      return new SnippetInjectingPrintWriter(super.getWriter(), SNIPPET, getCharacterEncoding());
    } else {
      return super.getWriter();
    }
  }
}
