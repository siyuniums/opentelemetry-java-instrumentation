/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.getOrCreateInjectionObject;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SnippetInjectingResponseWrapper extends HttpServletResponseWrapper {
  public static final String FAKE_SNIPPET_HEADER = "FAKE_SNIPPET_HEADER";
  private static final String SNIPPET = SnippetHolder.getSnippet();
  private static final int SNIPPET_LENGTH = SNIPPET.length();
  private SnippetInjectingPrintWriter snippetInjectingPrintWriter = null;
  @Nullable private static final MethodHandle setContentLengthLongHandler = getMethodHandle();

  public SnippetInjectingResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public boolean containsHeader(String name) {
    // override this function in order to make sure the response is wrapped
    // but not wrapped twice
    // we didn't use the traditional method req.setattribute
    // because async may carry out the original request without the attributes that we set
    if (name.equals(FAKE_SNIPPET_HEADER)) {
      return true;
    }
    return super.containsHeader(name);
  }

  @Override
  public void setHeader(String name, String value) {
    if (shouldInject() && "Content-Length".equalsIgnoreCase(name)) {
      try {
        value = Integer.toString(SNIPPET_LENGTH + Integer.valueOf(value));
      } catch (NumberFormatException ex) {
        System.out.println(ex);
        //        BuildLogger.Adapter logger = null;
        //        logger.debug("Invalid string format", ex);
      }
    }
    super.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    if (shouldInject() && "Content-Length".equalsIgnoreCase(name)) {
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
    if (shouldInject() && "Content-Length".equalsIgnoreCase(name)) {
      value += SNIPPET_LENGTH;
    }
    super.setIntHeader(name, value);
  }

  @Override
  public void addIntHeader(String name, int value) {
    if (shouldInject() && "Content-Length".equalsIgnoreCase(name)) {
      value += SNIPPET_LENGTH;
    }
    super.addIntHeader(name, value);
  }

  @Override
  public void setContentLength(int len) {
    if (shouldInject()) {
      len += SNIPPET_LENGTH;
    }
    super.setContentLength(len);
  }

  @Nullable
  private static MethodHandle getMethodHandle() {
    try {
      return MethodHandles.lookup()
          .findSpecial(
              HttpServletResponseWrapper.class,
              "setContentLengthLong",
              MethodType.methodType(void.class),
              SnippetInjectingResponseWrapper.class);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      System.out.println("SnippetInjectingResponseWrapper setContentLengthLong e: " + e);
      return null;
    }
  }

  private boolean shouldInject() {
    String contentType = super.getContentType();
    if (contentType == null) {
      contentType = super.getHeader("content-type");
    }
    if (contentType == null || !contentType.startsWith("text/html")) {
      return false;
    }
    return true;
  }

  public void setContentLengthLong(long length) throws Throwable {
    if (shouldInject()) {
      length += SNIPPET_LENGTH;
    }
    if (setContentLengthLongHandler == null) {
      super.setContentLength((int) length);
    } else {
      setContentLengthLongHandler.invokeWithArguments(this, length);
    }
  }

  public String getCharacterEncodingHelper() {
    String characterEncoding = super.getCharacterEncoding();
    if (characterEncoding == null) {
      // The default character encoding for HTML5, and the one recommended by the World Wide Web
      // Consortium for all content, is UTF-8.
      // if user didn't set characterEncoding before sending back response,
      // then response wrapper would use the most common utf-8 to encode it.
      characterEncoding = "UTF-8";
    }
    return characterEncoding;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    ServletOutputStream output = super.getOutputStream();
    InjectionState state = getOrCreateInjectionObject(output, getCharacterEncodingHelper());
    if (state.getWrapper() == null || state.getWrapper() != this) { // now I am a new response
      state.reset(this);
    }
    return output;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (!shouldInject()) {
      return super.getWriter();
    }
    if (snippetInjectingPrintWriter == null) {
      snippetInjectingPrintWriter =
          new SnippetInjectingPrintWriter(super.getWriter(), SNIPPET, getCharacterEncodingHelper());
    }
    return snippetInjectingPrintWriter;
  }
}
