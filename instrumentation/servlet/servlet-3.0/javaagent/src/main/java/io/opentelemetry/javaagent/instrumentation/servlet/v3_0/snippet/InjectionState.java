package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

public class InjectionState {
  private static final int ALREADY_INJECTED_FAKE_VALUE = -1;
  private static final int HEAD_TAG_LENGTH = "<head>".length();
  private int headTagBytesSeen = 0;
  private final String characterEncoding;

  public int getHeadTagBytesSeen() {
    return headTagBytesSeen;
  }

  public void resetHeadTagBytesSeen() {
    headTagBytesSeen = 0;
  }

  public String getCharacterEncoding() {
    return this.characterEncoding;
  }

  public SnippetInjectingResponseWrapper getWrapper() {
    return wrapper;
  }

  private SnippetInjectingResponseWrapper wrapper;

  public InjectionState(String characterEncoding) {
    this.characterEncoding = characterEncoding;
  }

  public void setAlreadyInjected() {
    headTagBytesSeen = ALREADY_INJECTED_FAKE_VALUE;
  }

  public boolean isAlreadyInjected() {
    return headTagBytesSeen == ALREADY_INJECTED_FAKE_VALUE;
  }
  /**
   * return false means injected happened, no need to monitor head tag any more return; return true
   * means "<head>" has shown and now is the right time to inject
   */
  public boolean processByte(byte b) {
    if (isAlreadyInjected()) {
      return false;
    }
    if (stillInHeadTag(b)) {
      headTagBytesSeen++;
    } else {
      headTagBytesSeen = 0;
    }
    return headTagBytesSeen == HEAD_TAG_LENGTH;
  }

  public boolean stillInHeadTag(byte b) {
    if (headTagBytesSeen == 0 && b == '<') {
      return true;
    } else if (headTagBytesSeen == 1 && b == 'h') {
      return true;
    } else if (headTagBytesSeen == 2 && b == 'e') {
      return true;
    } else if (headTagBytesSeen == 3 && b == 'a') {
      return true;
    } else if (headTagBytesSeen == 4 && b == 'd') {
      return true;
    } else if (headTagBytesSeen == 5 && b == '>') {
      return true;
    }
    return false;
  }

  public void setWrapper(SnippetInjectingResponseWrapper wrapper) {
    this.wrapper = wrapper;
  }
}
