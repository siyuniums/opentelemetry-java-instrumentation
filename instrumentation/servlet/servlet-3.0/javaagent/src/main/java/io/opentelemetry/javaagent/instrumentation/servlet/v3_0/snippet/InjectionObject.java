package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

public class InjectionObject {
  public int headTagBytesSeen = -1;
  public String characterEncoding;
  public SnippetInjectingResponseWrapper wrapper;

  public boolean inject() {
    return headTagBytesSeen == 5;
  }

  public boolean injected() {
    return headTagBytesSeen == -2;
  }

  public void intInjectionHelper(byte b) {
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

  public boolean intInjection(ServletOutputStream sp, byte b) throws IOException {
    intInjectionHelper(b);
    if (inject()) {
      headTagBytesSeen = -2;
      sp.write(b);
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(this.characterEncoding);
      sp.write(snippetBytes);
      if (wrapper.contentLength > 0) {
        System.out.println(
            "intInjection wrapper.contentLength > 0 "
                + (wrapper.contentLength + SnippetHolder.getSnippet().length()));
        wrapper.setHeader(
            "Content-Length",
            Long.toString(wrapper.contentLength + SnippetHolder.getSnippet().length()));
      } else {
        System.out.println("wrapper.contentLength <= 0 ");
        wrapper.addLength = true;
      }

      return false;
    } else {
      return true;
    }
  }

  public boolean stringInjection(ServletOutputStream sp, byte[] original, int off, int length)
      throws IOException {
    int i;
    for (i = off; i < length && i - off < length; i++) {
      intInjectionHelper(original[i]);
      if (inject()) {
        break;
      }
    }
    if (inject()) {
      headTagBytesSeen = -2;
      sp.write(original, off, i + 1);
      byte[] snippetBytes = SnippetHolder.getSnippetBytes(this.characterEncoding);
      sp.write(snippetBytes);
      sp.write(original, i + 1, length - i - 1);
      if (wrapper.contentLength > 0) {
        System.out.println(
            "stringInjection wrapper.contentLength > 0 "
                + Long.toString(wrapper.contentLength + SnippetHolder.getSnippet().length()));
        wrapper.setHeader(
            "Content-Length",
            Long.toString(wrapper.contentLength + SnippetHolder.getSnippet().length()));
        wrapper.setContentLength(185);
      } else {
        System.out.println("wrapper.contentLength <= 0 ");
        wrapper.addLength = true;
      }
      return false;
    } else {
      return true;
    }
  }
}
