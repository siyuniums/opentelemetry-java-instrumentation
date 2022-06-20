/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.servlet.common.service;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ServletAndFilterInstrumentation implements TypeInstrumentation {
  private final String basePackageName;
  private final String outPutClassName;
  private final String adviceClassName;
  private final String servletInitAdviceClassName;
  private final String filterInitAdviceClassName;

  public ServletAndFilterInstrumentation(
      String basePackageName,
      String outPutClassName,
      String adviceClassName,
      String servletInitAdviceClassName,
      String filterInitAdviceClassName) {
    this.basePackageName = basePackageName;
    this.outPutClassName = outPutClassName;
    this.adviceClassName = adviceClassName;
    this.servletInitAdviceClassName = servletInitAdviceClassName;
    this.filterInitAdviceClassName = filterInitAdviceClassName;
    System.out.println("--------------ServletAndFilterInstrumentation-------" + outPutClassName);
  }

  public ServletAndFilterInstrumentation(String basePackageName, String adviceClassName) {
    this(basePackageName, null, adviceClassName , null, null);
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return hasClassesNamed(basePackageName + ".Servlet");
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    System.out.println("typeMatcher--------------ServletAndFilterInstrumentation------- typeMatcher" );
    return hasSuperType(namedOneOf(basePackageName + ".Filter", basePackageName + ".Servlet", basePackageName + ".ServletOutputStream"));
  }

  @Override
  public void transform(TypeTransformer transformer) {
    System.out.println("--------------ServletAndFilterInstrumentation transform-------");
    if (outPutClassName != null) {
      transformer.applyAdviceToMethod(
          named("write")
              .and(isPublic()),
          outPutClassName);
    }

    transformer.applyAdviceToMethod(
        namedOneOf("doFilter", "service")
            .and(takesArgument(0, named(basePackageName + ".ServletRequest")))
            .and(takesArgument(1, named(basePackageName + ".ServletResponse")))
            .and(isPublic()),
        adviceClassName);
    if (servletInitAdviceClassName != null) {
      transformer.applyAdviceToMethod(
          named("init").and(takesArgument(0, named(basePackageName + ".ServletConfig"))),
          servletInitAdviceClassName);
    }
    if (filterInitAdviceClassName != null) {
      transformer.applyAdviceToMethod(
          named("init").and(takesArgument(0, named(basePackageName + ".FilterConfig"))),
          filterInitAdviceClassName);
    }
  }
}
