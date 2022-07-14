package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class MyPrinterWriterTest {

  public String readFile(String resourceName) throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString("UTF-8");
  }

  @Test
  public void shouldInjectToTextHtml() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");
    // read the correct answer
    String correct = readFile("staticHtmlAfter.html");
    StringWriter writer = new StringWriter();

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("text/html");
    when(response.getOutputStream())
        .thenReturn(
            new ServletOutputStream() {
              @Override
              public void write(int b) {
                if (b > 0) {
                  writer.write(b);
                }
              }
            });
    SnippetHolder.setSnippet("\n  <script type=\"text/javascript\"> Test </script>");
    SnippetInjectingResponseWrapper responseWrapper = new SnippetInjectingResponseWrapper(response);
    responseWrapper.getOutputStream().write(original.getBytes("UTF-16"));

    // check whether new response == correct answer
    String result = writer.toString();
    writer.close();
    assertThat(result).isEqualTo(correct);
  }

  @Test
  public void shouldNotInjectToTextHtml() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");

    StringWriter writer = new StringWriter();

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("notText");
    when(response.getOutputStream())
        .thenReturn(
            new ServletOutputStream() {
              @Override
              public void write(int b) {
                if (b > 0) {
                  writer.write(b);
                }
              }
            });
    SnippetHolder.setSnippet("\n  <script type=\"text/javascript\"> Test </script>");
    SnippetInjectingResponseWrapper responseWrapper = new SnippetInjectingResponseWrapper(response);
    responseWrapper.getOutputStream().write(original.getBytes("UTF-16"));

    // check whether new response == correct answer
    String result = writer.toString();
    writer.close();
    assertThat(result).isEqualTo(original);
  }

  @Test
  public void testPrinterWriter() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");
    // read the correct answer
    String correct = readFile("staticHtmlAfter.html");

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("text/html");

    StringWriter writer = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(writer, true));
    SnippetHolder.setSnippet("\n  <script type=\"text/javascript\"> Test </script>");
    SnippetInjectingResponseWrapper responseWrapper = new SnippetInjectingResponseWrapper(response);
    responseWrapper.getWriter().write(original);
    responseWrapper.getWriter().flush();
    responseWrapper.getWriter().close();

    // read file get result
    String result = writer.toString();
    writer.close();
    // check whether new response == correct answer
    assertThat(result).isEqualTo(correct);
  }
}
