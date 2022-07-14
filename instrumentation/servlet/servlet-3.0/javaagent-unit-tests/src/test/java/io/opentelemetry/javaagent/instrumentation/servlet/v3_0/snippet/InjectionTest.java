package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import static io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet.Injection.stringInjection;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

public class InjectionTest {
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
  public void testInjectionForStringContainHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the originalFile
    String original = readFile("staticHtmlOrigin.html");
    // read the correct answer
    String correct = readFile("staticHtmlAfter.html");
    byte[] originalBytes = original.getBytes(Charset.defaultCharset());
    InjectionObject obj = new InjectionObject();
    stringInjection(originalBytes, 0, originalBytes.length, obj);
    assertThat(obj.headTag).isEqualTo(-2);
    assertThat(obj.bits).isEqualTo(correct.getBytes(Charset.defaultCharset()));
    assertThat(obj.length)
        .isEqualTo(originalBytes.length + testSnippet.getBytes(Charset.defaultCharset()).length);
    assertThat(obj.length).isEqualTo(correct.getBytes(Charset.defaultCharset()).length);
  }

  @Test
  public void testInjectionForStringWithoutHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the originalFile
    String original = readFile("htmlWithoutHeadTag.html");

    byte[] originalBytes = original.getBytes(Charset.defaultCharset());
    InjectionObject obj = new InjectionObject();
    stringInjection(originalBytes, 0, originalBytes.length, obj);
    assertThat(obj.headTag).isEqualTo(-1);
    assertThat(obj.bits).isEqualTo(original.getBytes(Charset.defaultCharset()));
    assertThat(obj.length).isEqualTo(originalBytes.length);
  }

  @Test
  public void testHalfHeadTag() throws IOException {
    String testSnippet = "\n  <script type=\"text/javascript\"> Test </script>";
    SnippetHolder.setSnippet(testSnippet);
    // read the original string
    String originalFirstPart = "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<he";
    byte[] originalFirstPartBytes = originalFirstPart.getBytes(Charset.defaultCharset());
    InjectionObject obj = new InjectionObject();
    stringInjection(originalFirstPartBytes, 0, originalFirstPartBytes.length, obj);
    assertThat(obj.headTag).isEqualTo(2);
    assertThat(obj.bits).isEqualTo(originalFirstPart.getBytes(Charset.defaultCharset()));
    assertThat(obj.length).isEqualTo(originalFirstPartBytes.length);

    String originalSecondPart =
        "ad>\n"
            + "  <meta charset=\"UTF-8\">\n"
            + "  <title>Title</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "\n"
            + "</body>\n"
            + "</html>";
    byte[] originalSecondPartBytes = originalSecondPart.getBytes(Charset.defaultCharset());
    stringInjection(originalSecondPartBytes, 0, originalSecondPartBytes.length, obj);
    assertThat(obj.headTag).isEqualTo(-2);

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

    assertThat(new String(obj.bits, Charset.defaultCharset())).isEqualTo(correctSecondPart);
    assertThat(obj.length)
        .isEqualTo(
            originalSecondPartBytes.length + testSnippet.getBytes(Charset.defaultCharset()).length);
  }
}
