/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

public class InjectionState {
  private static final int ALREADY_INJECTED_FAKE_VALUE = -1;
  private static final int HEAD_TAG_LENGTH = "<head>".length();

  private final String characterEncoding;
  private SnippetInjectingResponseWrapper wrapper;
  private int headTagBytesSeen = 0;

  public InjectionState(String characterEncoding) {
    this.characterEncoding = characterEncoding;
  }

  public int getHeadTagBytesSeen() {
    return headTagBytesSeen;
  }

  public String getCharacterEncoding() {
    return this.characterEncoding;
  }

  public SnippetInjectingResponseWrapper getWrapper() {
    return wrapper;
  }

  public void setAlreadyInjected() {
    headTagBytesSeen = ALREADY_INJECTED_FAKE_VALUE;
  }

  public boolean isAlreadyInjected() {
    return headTagBytesSeen == ALREADY_INJECTED_FAKE_VALUE;
  }
  /**
   * Returns true when the byte is the last character of "<head>" and now is the right time to
   * inject. Otherwise, returns false.
   */
  public boolean processByte(int b) {
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

  private boolean stillInHeadTag(int b) {
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
}
