package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;

public class InjectionObject {
  public int headTagBytesSeen = -1;
  public String characterEncoding;

  public boolean inject() {
    return headTagBytesSeen == 5;
  }

  public void intInjection(byte b) {
    int headTagBytesSeen = this.headTagBytesSeen;
    if (headTagBytesSeen == -1 && b == '<') {
      headTagBytesSeen = 0;
    } else if (headTagBytesSeen == 0 && b == 'h') {
      headTagBytesSeen = 1;
    } else if (headTagBytesSeen == 1 && b == 'e') {
      headTagBytesSeen = 2;
    } else if (headTagBytesSeen == 2 && b == 'a') {
      headTagBytesSeen = 3;
    } else if (headTagBytesSeen == 3 && b == 'd') {
      headTagBytesSeen = 4;
    } else if (headTagBytesSeen == 4 && b == '>') {
      headTagBytesSeen = 5;
    } else if (b > 0 && headTagBytesSeen != -2) {
      headTagBytesSeen = -1;
    }
    this.headTagBytesSeen = headTagBytesSeen;
  }

  @Nullable
  public InjectedInfo stringInjection(byte[] original, int off, int length)
      throws UnsupportedEncodingException {
    for (int i = off; i < original.length && i - off < length; i++) {
      intInjection(original[i]);
      if (this.inject()) {
        byte[] snippetBytes = SnippetHolder.getSnippetBytes(this.characterEncoding);
        byte[] buffer = new byte[length + snippetBytes.length];
        System.arraycopy(original, off, buffer, 0, i + 1);
        System.arraycopy(snippetBytes, 0, buffer, i + 1, snippetBytes.length);
        System.arraycopy(
            original, i + 1, buffer, i + 1 + snippetBytes.length, original.length - i - 1);
        this.headTagBytesSeen = -2;
        InjectedInfo info = new InjectedInfo();
        info.bits = buffer;
        info.length = length + snippetBytes.length;
        return info;
      }
    }
    return null;
  }
}
