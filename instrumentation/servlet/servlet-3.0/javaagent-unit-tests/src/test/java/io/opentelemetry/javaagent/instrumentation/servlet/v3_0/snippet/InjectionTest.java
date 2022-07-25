package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.ServletOutputStreamInjectionHelper.process;
import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.TestUtil.readFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletOutputStream;
import org.junit.jupiter.api.Test;

class InjectionTest {

  @Test
  void testInjectionForStringContainHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");
    // read the correct answer
    String correct = readFile("staticHtmlAfter.html");
    byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
    InjectionState obj = new InjectionState();

    StringWriter writer = new StringWriter();

    ServletOutputStream sp =
        new ServletOutputStream() {
          @Override
          public void write(int b) throws IOException {
            writer.write(b);
          }
        };
    boolean injected = !process(obj, sp, originalBytes, 0, originalBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(-2);
    assertThat(injected).isEqualTo(true);
    writer.flush();

    String result = writer.toString();
    writer.close();
    assertThat(result).isEqualTo(correct);
  }

  //  @Test
  //  void testInjectionForChinese() throws IOException {
  //    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
  //    SnippetHolder.setSnippet(testSnippet);
  //    // read the originalFile
  //    String original = readFile("staticHtmlChineseOrigin.html");
  //    // read the correct answer
  //    String correct = readFile("staticHtmlChineseAfter.html");
  //    byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
  //    InjectionObject obj = new InjectionObject();
  //
  //    StringWriter writer = new StringWriter();
  //
  //    ServletOutputStream sp =
  //        new ServletOutputStream() {
  //          @Override
  //          public void write(int b) throws IOException {
  //            writer.write(b);
  //          }
  //        };
  //    boolean injected = !obj.stringInjection(sp, originalBytes, 0, originalBytes.length);
  //    assertThat(obj.headTagBytesSeen).isEqualTo(-2);
  //    assertThat(injected).isEqualTo(true);
  //    writer.flush();
  //
  //    String result = writer.toString();
  //    writer.close();
  //    assertThat(result).isEqualTo(correct);
  //  }

  @Test
  void testInjectionForStringWithoutHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the originalFile
    String original = readFile("htmlWithoutHeadTag.html");

    byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
    InjectionState obj = new InjectionState();
    StringWriter writer = new StringWriter();

    ServletOutputStream sp =
        new ServletOutputStream() {
          @Override
          public void write(int b) throws IOException {
            writer.write(b);
          }
        };
    boolean injected = !process(obj, sp, originalBytes, 0, originalBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(-1);
    assertThat(injected).isEqualTo(false);
    writer.flush();
    String result = writer.toString();
    writer.close();
    assertThat(result).isEqualTo("");
  }

  @Test
  void testHalfHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the original string
    String originalFirstPart = "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<he";
    byte[] originalFirstPartBytes = originalFirstPart.getBytes(StandardCharsets.UTF_8);
    InjectionState obj = new InjectionState();
    StringWriter writer = new StringWriter();

    ServletOutputStream sp =
        new ServletOutputStream() {
          @Override
          public void write(int b) throws IOException {
            writer.write(b);
          }
        };
    boolean injected = !process(obj, sp, originalFirstPartBytes, 0, originalFirstPartBytes.length);

    writer.flush();
    String result = writer.toString();
    assertThat(obj.headTagBytesSeen).isEqualTo(2);
    assertThat(result).isEqualTo("");
    assertThat(injected).isEqualTo(false);
    String originalSecondPart =
        "ad>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <title>Title</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "\n"
            + "</body>\n"
            + "</html>";
    byte[] originalSecondPartBytes = originalSecondPart.getBytes(StandardCharsets.UTF_8);
    injected = !process(obj, sp, originalSecondPartBytes, 0, originalSecondPartBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(-2);
    assertThat(injected).isEqualTo(true);
    String correctSecondPart =
        "ad>\n"
            + "  <script type=\"text/javascript\"> Test </script>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <title>Title</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "\n"
            + "</body>\n"
            + "</html>";
    writer.flush();
    result = writer.toString();
    assertThat(result).isEqualTo(correctSecondPart);
  }
}
