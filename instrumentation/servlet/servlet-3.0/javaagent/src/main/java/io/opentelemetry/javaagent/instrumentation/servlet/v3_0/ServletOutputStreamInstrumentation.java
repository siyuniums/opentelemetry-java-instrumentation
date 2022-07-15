/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.v3_0;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ServletOutputStreamInstrumentation implements TypeInstrumentation {
  private final String basePackageName;
  private final String servlet3OutputStreamWriteBytesAndOffsetClassName;
  private final String servlet3OutputStreamWriteBytesClassName;
  private final String servlet3OutputStreamWriteIntAdviceClassName;

  public ServletOutputStreamInstrumentation(
      String basePackageName,
      String servlet3OutputStreamWriteBytesAndOffsetClassName,
      String servlet3OutputStreamWriteBytesClassName,
      String servlet3OutputStreamWriteIntAdviceClassName) {
    this.basePackageName = basePackageName;
    this.servlet3OutputStreamWriteBytesAndOffsetClassName =
        servlet3OutputStreamWriteBytesAndOffsetClassName;
    this.servlet3OutputStreamWriteBytesClassName = servlet3OutputStreamWriteBytesClassName;
    this.servlet3OutputStreamWriteIntAdviceClassName = servlet3OutputStreamWriteIntAdviceClassName;
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed(basePackageName + ".ServletOutputStream");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return hasSuperType(namedOneOf(basePackageName + ".ServletOutputStream"));
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("write")
            .and(takesArguments(3))
            .and(takesArgument(0, byte[].class))
            .and(takesArgument(1, int.class))
            .and(takesArgument(2, int.class))
            .and(isPublic()),
        servlet3OutputStreamWriteBytesAndOffsetClassName);
    transformer.applyAdviceToMethod(
        named("write").and(takesArgument(0, byte[].class)).and(isPublic()),
        servlet3OutputStreamWriteBytesClassName);
    transformer.applyAdviceToMethod(
        named("write").and(takesArgument(0, int.class)).and(isPublic()),
        servlet3OutputStreamWriteIntAdviceClassName);
  }
}
