/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.extension.matcher;

import io.opentelemetry.instrumentation.api.internal.cache.Cache;
import io.opentelemetry.javaagent.bootstrap.internal.ClassLoaderMatcherCacheHolder;
import io.opentelemetry.javaagent.bootstrap.internal.InClassLoaderMatcher;
import net.bytebuddy.matcher.ElementMatcher;

class ClassLoaderHasClassesNamedMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

  private final Cache<ClassLoader, Boolean> cache = Cache.weak();

  private final String[] resources;

  ClassLoaderHasClassesNamedMatcher(String... classNames) {
    resources = classNames;
    for (int i = 0; i < resources.length; i++) {
      resources[i] = resources[i].replace(".", "/") + ".class";
    }
    ClassLoaderMatcherCacheHolder.addCache(cache);
  }

  @Override
  public boolean matches(ClassLoader cl) {
    if (cl == null) {
      // Can't match the bootstrap class loader.
      return false;
    }
    return cache.computeIfAbsent(cl, this::hasResources);
  }

  private boolean hasResources(ClassLoader cl) {
    boolean priorValue = InClassLoaderMatcher.getAndSet(true);
    try {
      for (String resource : resources) {
        if (cl.getResource(resource) == null) {
          return false;
        }
      }
    } finally {
      InClassLoaderMatcher.set(priorValue);
    }
    return true;
  }
}
