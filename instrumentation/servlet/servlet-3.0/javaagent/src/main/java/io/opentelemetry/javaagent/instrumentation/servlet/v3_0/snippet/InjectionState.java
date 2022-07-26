package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

public class InjectionState {
  private static final int ALREADY_INJECTED_FAKE_VALUE = -2;
  public int headTagBytesSeen = -1;
  public String characterEncoding;
  public SnippetInjectingResponseWrapper wrapper;

  public InjectionState(String characterEncoding) {
    this.characterEncoding = characterEncoding;
    this.headTagBytesSeen = -1;
  }

  public InjectionState() {
    this.headTagBytesSeen = -1;
  }

  public void setAlreadyInjected() {
    headTagBytesSeen = ALREADY_INJECTED_FAKE_VALUE;
  }

  public boolean alreadyInjected() {
    return headTagBytesSeen == ALREADY_INJECTED_FAKE_VALUE;
  }

  public boolean processByte(byte b) {
    if (alreadyInjected()) { // headTagBytesSeen = -2;
      return false;
    }
    if (stillInHeadTag(b)) {
      headTagBytesSeen++;
    } else {
      headTagBytesSeen = -1;
    }
    return headTagBytesSeen == 5;
  }

  public boolean stillInHeadTag(byte b) {
    if (headTagBytesSeen == -1 && b == '<') {
      return true;
    } else if (headTagBytesSeen == 0 && b == 'h') {
      return true;
    } else if (headTagBytesSeen == 1 && b == 'e') {
      return true;
    } else if (headTagBytesSeen == 2 && b == 'a') {
      return true;
    } else if (headTagBytesSeen == 3 && b == 'd') {
      return true;
    } else if (headTagBytesSeen == 4 && b == '>') {
      return true;
    } else if (b > 0 && headTagBytesSeen != -2) {
      return false;
    }
    return false;
  }
}
