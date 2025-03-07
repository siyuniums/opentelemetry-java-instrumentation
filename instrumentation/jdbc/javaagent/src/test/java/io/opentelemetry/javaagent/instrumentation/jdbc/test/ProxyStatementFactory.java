/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jdbc.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Statement;

public class ProxyStatementFactory {

  public static Statement proxyStatement(Statement statement) throws Exception {
    TestClassLoader classLoader = new TestClassLoader(ProxyStatementFactory.class.getClassLoader());
    Class<?> testInterface = classLoader.loadClass(TestInterface.class.getName());
    if (testInterface.getClassLoader() != classLoader) {
      throw new IllegalStateException("wrong class loader");
    }
    InvocationHandler invocationHandler = (proxy, method, args) -> method.invoke(statement, args);
    Statement proxyStatement =
        (Statement)
            Proxy.newProxyInstance(
                classLoader, new Class<?>[] {Statement.class, testInterface}, invocationHandler);
    // adding package private interface TestInterface to jdk proxy forces defining the proxy class
    // in the same package as the package private interface
    if (!proxyStatement
        .getClass()
        .getName()
        .startsWith("io.opentelemetry.javaagent.instrumentation.jdbc.test")) {
      throw new IllegalStateException("proxy statement is in wrong package");
    }

    return proxyStatement;
  }
}
