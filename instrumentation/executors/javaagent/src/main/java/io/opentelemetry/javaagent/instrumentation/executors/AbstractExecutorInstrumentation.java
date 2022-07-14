/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.executors;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.named;

import io.opentelemetry.javaagent.bootstrap.internal.InstrumentationConfig;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public abstract class AbstractExecutorInstrumentation implements TypeInstrumentation {
  private static final Logger logger =
      Logger.getLogger(AbstractExecutorInstrumentation.class.getName());

  private static final String EXECUTORS_INCLUDE_PROPERTY_NAME =
      "otel.instrumentation.executors.include";

  private static final String EXECUTORS_INCLUDE_ALL_PROPERTY_NAME =
      "otel.instrumentation.executors.include-all";

  private static final boolean INCLUDE_ALL =
      InstrumentationConfig.get().getBoolean(EXECUTORS_INCLUDE_ALL_PROPERTY_NAME, false);

  /**
   * Only apply executor instrumentation to allowed executors. To apply to all executors, use
   * override setting above.
   */
  private final Collection<String> includeExecutors;

  /**
   * Some frameworks have their executors defined as anon classes inside other classes. Referencing
   * anon classes by name would be fragile, so instead we will use list of class prefix names. Since
   * checking this list is more expensive (O(n)) we should try to keep it short.
   */
  private final Collection<String> includePrefixes;

  protected AbstractExecutorInstrumentation() {
    if (INCLUDE_ALL) {
      includeExecutors = Collections.emptyList();
      includePrefixes = Collections.emptyList();
    } else {
      String[] includeExecutors = {
        "akka.actor.ActorSystemImpl$$anon$1",
        "akka.dispatch.BalancingDispatcher",
        "akka.dispatch.Dispatcher",
        "akka.dispatch.Dispatcher$LazyExecutorServiceDelegate",
        "akka.dispatch.ExecutionContexts$sameThreadExecutionContext$",
        "akka.dispatch.forkjoin.ForkJoinPool",
        "akka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinPool",
        "akka.dispatch.MessageDispatcher",
        "akka.dispatch.PinnedDispatcher",
        "com.google.common.util.concurrent.AbstractListeningExecutorService",
        "com.google.common.util.concurrent.MoreExecutors$ListeningDecorator",
        "com.google.common.util.concurrent.MoreExecutors$ScheduledListeningDecorator",
        "io.netty.channel.epoll.EpollEventLoop",
        "io.netty.channel.epoll.EpollEventLoopGroup",
        "io.netty.channel.MultithreadEventLoopGroup",
        "io.netty.channel.nio.NioEventLoop",
        "io.netty.channel.nio.NioEventLoopGroup",
        "io.netty.channel.SingleThreadEventLoop",
        "io.netty.util.concurrent.AbstractEventExecutor",
        "io.netty.util.concurrent.AbstractEventExecutorGroup",
        "io.netty.util.concurrent.AbstractScheduledEventExecutor",
        "io.netty.util.concurrent.DefaultEventExecutor",
        "io.netty.util.concurrent.DefaultEventExecutorGroup",
        "io.netty.util.concurrent.GlobalEventExecutor",
        "io.netty.util.concurrent.MultithreadEventExecutorGroup",
        "io.netty.util.concurrent.SingleThreadEventExecutor",
        "java.util.concurrent.AbstractExecutorService",
        "java.util.concurrent.CompletableFuture$ThreadPerTaskExecutor",
        "java.util.concurrent.Executors$DelegatedExecutorService",
        "java.util.concurrent.Executors$FinalizableDelegatedExecutorService",
        "java.util.concurrent.ForkJoinPool",
        "java.util.concurrent.ScheduledThreadPoolExecutor",
        "java.util.concurrent.ThreadPoolExecutor",
        "org.apache.tomcat.util.threads.ThreadPoolExecutor",
        "org.eclipse.jetty.util.thread.QueuedThreadPool", // dispatch() covered in the jetty module
        "org.eclipse.jetty.util.thread.ReservedThreadExecutor",
        "org.glassfish.grizzly.threadpool.GrizzlyExecutorService",
        "org.jboss.threads.EnhancedQueueExecutor",
        "play.api.libs.streams.Execution$trampoline$",
        "play.shaded.ahc.io.netty.util.concurrent.ThreadPerTaskExecutor",
        "scala.concurrent.forkjoin.ForkJoinPool",
        "scala.concurrent.Future$InternalCallbackExecutor$",
        "scala.concurrent.impl.ExecutionContextImpl",
      };
      Set<String> combined = new HashSet<>(Arrays.asList(includeExecutors));
      combined.addAll(
          InstrumentationConfig.get().getList(EXECUTORS_INCLUDE_PROPERTY_NAME, emptyList()));
      this.includeExecutors = Collections.unmodifiableSet(combined);

      String[] includePrefixes = {"slick.util.AsyncExecutor$"};
      this.includePrefixes = Collections.unmodifiableCollection(Arrays.asList(includePrefixes));
    }
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    ElementMatcher.Junction<TypeDescription> matcher = any();
    ElementMatcher.Junction<TypeDescription> hasExecutorInterfaceMatcher =
        implementsInterface(named(Executor.class.getName()));
    if (!INCLUDE_ALL) {
      matcher =
          matcher.and(
              new ElementMatcher<TypeDescription>() {
                @Override
                public boolean matches(TypeDescription target) {
                  boolean allowed = includeExecutors.contains(target.getName());

                  // Check for possible prefixes match only if not allowed already
                  if (!allowed) {
                    for (String name : includePrefixes) {
                      if (target.getName().startsWith(name)) {
                        allowed = true;
                        break;
                      }
                    }
                  }

                  if (!allowed && hasExecutorInterfaceMatcher.matches(target)) {
                    logger.log(FINE, "Skipping executor instrumentation for {0}", target.getName());
                  }
                  return allowed;
                }
              });
    }
    return matcher.and(hasExecutorInterfaceMatcher); // Apply expensive matcher last.
  }
}
