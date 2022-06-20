/* SPDX-License-Identifier: Apache-2.0 */
package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import net.bytebuddy.asm.Advice;
import java.nio.charset.Charset;
import java.util.Arrays;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class Servlet3OutputStream{

    private static final String SNIPPET =
        "<script type=\"text/javascript\">\n"
            + " function msg(){ alert(\"TEST TEST\");}\n"
            + "</script> \n";

  @Advice.OnMethodEnter(suppress = Throwable.class)
  @SuppressWarnings("SystemOut")
  public static void onEnter(
      @Advice.AllArguments(readOnly = false, typing = DYNAMIC) Object[] args
  ) {
    System.out.println("_____________get out put stream" + Arrays.toString(args));
    for (int i = 0; i < args.length; i++){
      System.out.println("get " + i + " " + args[i]);
    }
    System.out.println(args[0].getClass());
//    System.out.println("args[0] length " + args[0].length);
    String s = new String((byte[]) args[0], Charset.defaultCharset());

    if (s.contains("<head>")){
      int offset = s.indexOf("</head>");
      String news = s.substring(0, offset) + SNIPPET + s.substring(offset);


      Object[] argsCopy = new Object[3];
      argsCopy[0] = news.getBytes(Charset.defaultCharset());
      argsCopy[1] = args[1];
      argsCopy[2] = ((byte[]) args[0]).length;
      args = argsCopy;

      System.out.println("args 0 is now" + new String((byte[]) args[0], Charset.defaultCharset()));
      System.out.println("args length is now" + args[2]);
    }


  }
}
