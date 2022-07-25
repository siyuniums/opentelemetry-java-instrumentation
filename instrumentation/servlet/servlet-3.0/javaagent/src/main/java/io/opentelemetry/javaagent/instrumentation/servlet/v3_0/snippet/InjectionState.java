package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

public class InjectionState {

  public int headTagBytesSeen = -1;
  public String characterEncoding;
  public SnippetInjectingResponseWrapper wrapper;
  public static final int ALREADY_INJECTED_FAKE_VALUE = -2;

  public boolean shouldInject() {
    return headTagBytesSeen == 5;
  }

  boolean alreadyInjected() {
    return headTagBytesSeen == ALREADY_INJECTED_FAKE_VALUE;
  }

  public boolean processByte(byte b) {
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
    return shouldInject();
  }
}
