/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.testing;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractSpringKafkaTest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractSpringKafkaTest.class);

  static KafkaContainer kafka;

  ConfigurableApplicationContext applicationContext;
  protected KafkaTemplate<String, String> kafkaTemplate;

  @BeforeAll
  static void setUpKafka() {
    kafka =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))
            .waitingFor(Wait.forLogMessage(".*started \\(kafka.server.KafkaServer\\).*", 1))
            .withStartupTimeout(Duration.ofMinutes(1));
    kafka.start();
  }

  @AfterAll
  static void tearDownKafka() {
    kafka.stop();
  }

  protected abstract InstrumentationExtension testing();

  protected abstract List<Class<?>> additionalSpringConfigs();

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpApp() {
    Map<String, Object> props = new HashMap<>();
    props.put("spring.jmx.enabled", false);
    props.put("spring.main.web-application-type", "none");
    props.put("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
    props.put("spring.kafka.consumer.auto-offset-reset", "earliest");
    props.put("spring.kafka.consumer.linger-ms", 10);
    // wait 1s between poll() calls
    props.put("spring.kafka.listener.idle-between-polls", 1000);
    props.put("spring.kafka.producer.transaction-id-prefix", "test-");

    SpringApplication app = new SpringApplication(ConsumerConfig.class);
    app.addPrimarySources(additionalSpringConfigs());
    app.setDefaultProperties(props);
    applicationContext = app.run();
    kafkaTemplate = applicationContext.getBean("kafkaTemplate", KafkaTemplate.class);
  }

  @AfterEach
  void tearDownApp() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }

  protected void sendBatchMessages(Map<String, String> keyToData) throws InterruptedException {
    // This test assumes that messages are sent and received as a batch. Occasionally it happens
    // that the messages are not received as a batch, but one by one. This doesn't match what the
    // assertion expects. To reduce flakiness we retry the test when messages weren't received as
    // a batch.
    int maxAttempts = 5;
    for (int i = 1; i <= maxAttempts; i++) {
      BatchRecordListener.reset();

      testing()
          .runWithSpan(
              "producer",
              () -> {
                kafkaTemplate.executeInTransaction(
                    ops -> {
                      keyToData.forEach((key, data) -> ops.send("testBatchTopic", key, data));
                      return 0;
                    });
              });

      BatchRecordListener.waitForMessages();
      if (BatchRecordListener.getLastBatchSize() == 2) {
        break;
      } else if (i < maxAttempts) {
        testing().waitForTraces(2);
        Thread.sleep(1_000); // sleep a bit to give time for all the spans to arrive
        testing().clearData();
        logger.info("Messages weren't received as batch, retrying");
      }
    }
  }

  protected static Consumer<List<? extends LinkData>> links(SpanContext... spanContexts) {
    return links -> {
      assertThat(links).hasSize(spanContexts.length);
      for (SpanContext spanContext : spanContexts) {
        assertThat(links)
            .anySatisfy(
                link -> {
                  assertThat(link.getSpanContext().getTraceId())
                      .isEqualTo(spanContext.getTraceId());
                  assertThat(link.getSpanContext().getSpanId()).isEqualTo(spanContext.getSpanId());
                  assertThat(link.getSpanContext().getTraceFlags())
                      .isEqualTo(spanContext.getTraceFlags());
                  assertThat(link.getSpanContext().getTraceState())
                      .isEqualTo(spanContext.getTraceState());
                });
      }
    };
  }
}
