package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import org.junit.jupiter.api.Test;

class MyTest {

  @Test
  public void test() {
    System.out.println("hi");
    // MyPrintWriter
  }
}

/// * SPDX-License-Identifier: Apache-2.0 */
// package com.github.siyuniums;
//
//    import org.junit.jupiter.api.Test;
//    import javax.servlet.FilterChain;
//    import javax.servlet.ServletException;
//    import javax.servlet.ServletRequest;
//    import javax.servlet.ServletResponse;
//    import javax.servlet.http.HttpServletRequest;
//    import javax.servlet.http.HttpServletResponse;
//    import java.io.*;
//
//    import static org.assertj.core.api.Assertions.assertThat;
//    import static org.mockito.Mockito.mock;
//
// public class InjectFilterTest {
//
//  //    private InjectFilter injectFilter = new InjectFilter();
//  private HttpServletRequest request;
//  private HttpServletResponse response;
//
//  public String readFile(String resourceName){
//    String lines = "";
//    try {
//      InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
//
//      ByteArrayOutputStream result = new ByteArrayOutputStream();
//      byte[] buffer = new byte[1024];
//      for (int length; (length = is.read(buffer)) != -1; ) {
//        result.write(buffer, 0, length);
//      }
//      return result.toString("UTF-8");
//    } catch (IOException e) {
//    }
//    return lines;
//  }
//
//  public String removeEmpty(String original){
//    String finalResult = original.replaceAll(" ","");
//    finalResult = finalResult.replaceAll("\n", "");
//    finalResult = finalResult.replaceAll("\r", "");
//    return finalResult;
//
//  }
//
//
//  /**
//   * Mock response, create new printwriter for it
//   * wrapped up the response to make sure all the stuff would not be written to original output
// stream
//   * add a filter chain to write into the response
//   * do the filter, compare the new html with the correct one
//   * @throws IOException
//   * @throws ServletException
//   */
//
//  @Test
//  public void staticHTML() throws IOException, ServletException {
//
//    // read the injectionLine
//
//    String injectLine = readFile("injectJS.js");
//
//    // read the originalFile
//    String original = readFile("original/staticHtmlOrigin.html");
//
//    HttpServletRequest request = null;
//    // mock response
//    HttpServletResponse response1 = mock(HttpServletResponse.class);
//    MockHttpServletResponse response = new MockHttpServletResponse(response1);
//    response.setContentType("text/html");
//
//    // mock filterChain
//    // use another servlet to write into response
//    String finalOriginal = original;
//    FilterChain myChain = new FilterChain() {
//      @Override
//      public void doFilter(ServletRequest request, ServletResponse response) throws IOException,
// ServletException {
//        response.getWriter().print(finalOriginal);
//        response.getWriter().close();
//      }
//    };
//
//    // do the filter
////        injectFilter.doFilter(request, response, myChain);
//
//    // read the correct answer
//    String correct = readFile("afterFilterAnswer/staticHtmlAfter.html");
//
//    // check whether new response == correct answer
//    System.out.println("after Modified" + response.getOutPut());
//    correct = removeEmpty(correct);
//    String result = removeEmpty(response.getOutPut());
////        assertThat(result).isEqualTo(correct);
//  }
//
//
//  @Test
//  public void testResponseWithOutContentType() throws IOException, ServletException {
//
//    // read the injectionLine
//    String injectLine = readFile("injectJS.js");
//
//    // read the originalFile
//    String original = readFile("original/staticHtmlOrigin.html");
//
//    // mock response
//    HttpServletResponse response1 = mock(HttpServletResponse.class);
//    MockHttpServletResponse response = new MockHttpServletResponse(response1);
//
//    // mock filterChain
//    // use another servlet to write into response
//    String finalOriginal = original;
//    FilterChain myChain = new FilterChain() {
//      @Override
//      public void doFilter(ServletRequest request, ServletResponse response) throws IOException,
// ServletException {
//        response.getWriter().write(finalOriginal);
//        response.getWriter().close();
//      }
//    };
//    // do the filter
////        injectFilter.doFilter(request, response, myChain);
//
//    // read the correct answer
//    String correct = readFile("original/staticHtmlOrigin.html");
//
//    // check whether new response == correct answer
//    System.out.println("after Modified" + response.getOutPut());
////        assertThat(removeEmpty(response.getOutPut())).isEqualTo(removeEmpty(correct));
//  }
//
//  @Test
//  public void testGetOutputStream() throws IOException, ServletException {
//
//    // read the injectionLine
//
//    String injectLine = readFile("injectJS.js");
//
//    // read the originalFile
//    String original = readFile("original/staticHtmlOrigin.html");
//
//    HttpServletRequest request = null;
//    // mock response
//    HttpServletResponse response1 = mock(HttpServletResponse.class);
//    MockHttpServletResponse response = new MockHttpServletResponse(response1);
//    response.setContentType("text/html");
//
//    // mock filterChain
//    // use another servlet to write into response
//    String finalOriginal = original;
//    FilterChain myChain = new FilterChain() {
//      @Override
//      public void doFilter(ServletRequest request, ServletResponse response) throws IOException,
// ServletException {
//        response.getOutputStream().write(finalOriginal.getBytes());
//        response.getOutputStream().close();
//      }
//    };
//
//    // do the filter
////        injectFilter.doFilter(request, response, myChain);
//
//    // read the correct answer
//    String correct = readFile("afterFilterAnswer/staticHtmlAfter.html");
//
//    // check whether new response == correct answer
//    System.out.println("after Modified" + response.getOutPut());
//    correct = removeEmpty(correct);
//    String result = removeEmpty(response.getOutPut());
////        assertThat(result).isEqualTo(correct);
//  }
//
// }
