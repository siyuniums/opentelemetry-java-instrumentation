package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;

public class Injection {

  public static InjectionObject getInjectionObject(ServletOutputStream servletOutputStream) {

    InjectionObject virtualObj =
        VirtualField.find(ServletOutputStream.class, InjectionObject.class)
            .get(servletOutputStream);
    if (virtualObj == null) {
      virtualObj = new InjectionObject();
      setInjectionObject(servletOutputStream, virtualObj);
    }
    return virtualObj;
  }

  private static void setInjectionObject(
      ServletOutputStream servletOutputStream, InjectionObject obj) {
    VirtualField.find(ServletOutputStream.class, InjectionObject.class)
        .set(servletOutputStream, obj);
  }

  @Nullable
  public static InjectedInfo stringInjection(
      byte[] original, int off, int length, InjectionObject obj)
      throws UnsupportedEncodingException {
    InjectedInfo info = new InjectedInfo();
    info.bits = original;
    info.length = length;
    for (int i = off; i < original.length && i - off < length; i++) {
      intInjection(original[i], obj);
      if (obj.inject()) {
        byte[] snippetBytes = SnippetHolder.getSnippetBytes(obj.characterEncoding);
        byte[] buffer = new byte[length + snippetBytes.length];
        System.arraycopy(original, off, buffer, 0, i + 1);
        System.arraycopy(snippetBytes, 0, buffer, i + 1, snippetBytes.length);
        System.arraycopy(
            original, i + 1, buffer, i + 1 + snippetBytes.length, original.length - i - 1);
        obj.headTag = -2;
        info.bits = buffer;
        info.length = length + snippetBytes.length;
      }
    }
    return info;
  }

  public static void intInjection(byte b, InjectionObject injectObj) {
    int headTag = injectObj.headTag;
    if (headTag == -1 && b == '<') {
      headTag = 0;
    } else if (headTag == 0 && b == 'h') {
      headTag = 1;
    } else if (headTag == 1 && b == 'e') {
      headTag = 2;
    } else if (headTag == 2 && b == 'a') {
      headTag = 3;
    } else if (headTag == 3 && b == 'd') {
      headTag = 4;
    } else if (headTag == 4 && b == '>') {
      headTag = 5;
    } else if (b > 0 && headTag != -2) {
      headTag = -1;
    }
    injectObj.headTag = headTag;
  }
}
