package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

public class InjectionObject {
  public int headTag = -1;
  public String characterEncoding;

  public boolean inject() {
    return headTag == 5;
  }
}
