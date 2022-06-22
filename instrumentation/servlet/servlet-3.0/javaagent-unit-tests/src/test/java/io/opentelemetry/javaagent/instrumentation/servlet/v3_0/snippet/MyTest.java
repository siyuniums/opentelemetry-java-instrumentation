package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class MyTest {

  public String removeEmpty(String original) {
    String finalResult = original.replaceAll(" ", "");
    finalResult = finalResult.replaceAll("\n", "");
    finalResult = finalResult.replaceAll("\r", "");
    return finalResult;
  }

  public String readFile(String resourceName) {
    String lines = "";
    try {
      InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);

      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      for (int length; (length = is.read(buffer)) != -1; ) {
        result.write(buffer, 0, length);
      }
      return result.toString("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldInjectToTextHtml() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");
    // read the correct answer
    String correct = readFile("staticHtmlAfter.html");

    ArrayList<Character> list = new ArrayList<>();

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("text/html");
    when(response.getOutputStream())
        .thenReturn(
            new ServletOutputStream() {
              @Override
              public void write(int b) {
                list.add(char(b));
              }
            });

    MyResponseWrapper responseWrapper = new MyResponseWrapper(response);
    responseWrapper.getOutputStream().write(original.getBytes("UTF-16"));

    // check whether new response == correct answer
    correct = removeEmpty(correct);
    String result = list.toString();
    System.out.println("after Modified" + result);
    result = removeEmpty(result);
    assertThat(result).isEqualTo(correct);
  }
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

}
