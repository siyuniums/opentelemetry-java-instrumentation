package io.opentelemetry.javaagent.bootstrap.servlet;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.annotation.Nullable;

public class SnippetHolder {

  private static String snippet = "";

  public static void setSnippet(String snippet) {
    SnippetHolder.snippet = snippet;
  }

  public static String getSnippet() {
    return snippet;
  }

  public static byte[] getSnippetBytes(@Nullable String encoding)
      throws UnsupportedEncodingException {
    if (encoding != null) {
      return snippet.getBytes(encoding);
    } else {
      return snippet.getBytes(Charset.defaultCharset());
    }
  }
}
