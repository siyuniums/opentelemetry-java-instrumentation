package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.TestUtil.readFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

class SnippetInjectingResponseWrapperTest {

  @Test
  void testInjectToTextHtml() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");
    String correct = readFile("staticHtmlAfter.html");
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("text/html");

    StringWriter writer = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(writer));
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

  @Test
  void testInjectToChineseTextHtml() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlChineseOrigin.html");
    String correct = readFile("staticHtmlChineseAfter.html");
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("text/html");

    StringWriter writer = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(writer));
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

  @Test
  void shouldNotInjectToTextHtml() throws IOException {

    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");

    StringWriter writer = new StringWriter();
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getContentType()).thenReturn("not/text");

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
    assertThat(result).isEqualTo(original);
  }
}
