package io.opentelemetry.javaagent.instrumentation.servlet.v3_0.snippet;

import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder;
import java.io.UnsupportedEncodingException;
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

  public static void stringInjection(byte[] original, int off, int length, InjectionObject obj)
      throws UnsupportedEncodingException {
    obj.bits = original;
    obj.length = length;
    for (int i = off; i < original.length && i - off < length; i++) {
      intInjection(original[i], obj);
      if (obj.inject) {
        byte[] snippetBytes = SnippetHolder.getSnippetBytes(obj.characterEncoding);
        byte[] buffer = new byte[original.length + snippetBytes.length];
        System.arraycopy(original, 0, buffer, 0, i + 1);
        System.arraycopy(snippetBytes, 0, buffer, i + 1, snippetBytes.length);
        System.arraycopy(
            original, i + 1, buffer, i + 1 + snippetBytes.length, original.length - i - 1);
        obj.inject = false;
        obj.bits = buffer;
        obj.length = length + snippetBytes.length;
      }
    }
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
      injectObj.inject = true;
      headTag = -2; // inject happens
    } else if (b > 0 && headTag != -2) {
      headTag = -1;
    } else if (headTag == -2) {
      injectObj.inject = false;
    }
    injectObj.headTag = headTag;
  }
}
