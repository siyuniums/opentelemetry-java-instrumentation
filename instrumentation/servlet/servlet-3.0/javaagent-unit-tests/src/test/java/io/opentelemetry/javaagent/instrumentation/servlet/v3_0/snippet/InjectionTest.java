package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.TestUtil.readFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    InjectionObject obj = new InjectionObject();
    InjectedInfo info = obj.stringInjection(originalBytes, 0, originalBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(-2);
    assertThat(info.bits).isEqualTo(correct.getBytes(StandardCharsets.UTF_8));
    assertThat(info.length)
        .isEqualTo(originalBytes.length + testSnippet.getBytes(StandardCharsets.UTF_8).length);
    assertThat(info.length).isEqualTo(correct.getBytes(StandardCharsets.UTF_8).length);
  }

  @Test
  void testInjectionForStringWithoutHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the originalFile
    String original = readFile("htmlWithoutHeadTag.html");

    byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
    InjectionObject obj = new InjectionObject();
    InjectedInfo info = obj.stringInjection(originalBytes, 0, originalBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(-1);
    assertThat(info.bits).isEqualTo(original.getBytes(StandardCharsets.UTF_8));
    assertThat(info.length).isEqualTo(originalBytes.length);
  }

  @Test
  void testHalfHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the original string
    String originalFirstPart = "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<he";
    byte[] originalFirstPartBytes = originalFirstPart.getBytes(StandardCharsets.UTF_8);
    InjectionObject obj = new InjectionObject();
    InjectedInfo info =
        obj.stringInjection(originalFirstPartBytes, 0, originalFirstPartBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(2);
    assertThat(info.bits).isEqualTo(originalFirstPart.getBytes(StandardCharsets.UTF_8));
    assertThat(info.length).isEqualTo(originalFirstPartBytes.length);

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
    info = obj.stringInjection(originalSecondPartBytes, 0, originalSecondPartBytes.length);
    assertThat(obj.headTagBytesSeen).isEqualTo(-2);

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

    assertThat(new String(info.bits, StandardCharsets.UTF_8)).isEqualTo(correctSecondPart);
    assertThat(info.length)
        .isEqualTo(
            originalSecondPartBytes.length + testSnippet.getBytes(StandardCharsets.UTF_8).length);
  }
}
