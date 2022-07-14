/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.kafkaclients

import io.opentelemetry.instrumentation.test.LibraryTestTrait
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import spock.lang.Unroll

import java.time.Duration

import static io.opentelemetry.api.trace.SpanKind.CONSUMER
import static io.opentelemetry.api.trace.SpanKind.INTERNAL
import static io.opentelemetry.api.trace.SpanKind.PRODUCER

class InterceptorsTest extends KafkaClientBaseTest implements LibraryTestTrait {

  @Override
  Map<String, ?> producerProps() {
    def props = super.producerProps()
    props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.getName())
    return props
  }

  @Override
  Map<String, ?> consumerProps() {
    def props = super.consumerProps()
    props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.getName())
    return props
  }

  @Unroll
  def "test interceptors"() throws Exception {
    when:
    String greeting = "Hello Kafka!"
    runWithSpan("parent") {
      producer.send(new ProducerRecord(SHARED_TOPIC, greeting)) { meta, ex ->
        if (ex == null) {
          runWithSpan("producer callback") {}
        } else {
          runWithSpan("producer exception: " + ex) {}
        }
      }
    }

    then:
    awaitUntilConsumerIsReady()
    // check that the message was received
    def records = consumer.poll(Duration.ofSeconds(5).toMillis())
    records.count() == 1
    for (record in records) {
      assert record.value() == greeting
      assert record.key() == null
    }

    assertTraces(2) {
      traces.sort(orderByRootSpanKind(INTERNAL, PRODUCER, CONSUMER))
      trace(0, 3) {
        span(0) {
          name "parent"
          kind INTERNAL
          hasNoParent()
        }
        span(1) {
          name SHARED_TOPIC + " send"
          kind PRODUCER
          childOf span(0)
          attributes {
            "$SemanticAttributes.MESSAGING_SYSTEM" "kafka"
            "$SemanticAttributes.MESSAGING_DESTINATION" SHARED_TOPIC
            "$SemanticAttributes.MESSAGING_DESTINATION_KIND" "topic"
          }
        }
        span(2) {
          name SHARED_TOPIC + " receive"
          kind CONSUMER
          childOf span(1)
          attributes {
            "$SemanticAttributes.MESSAGING_SYSTEM" "kafka"
            "$SemanticAttributes.MESSAGING_DESTINATION" SHARED_TOPIC
            "$SemanticAttributes.MESSAGING_DESTINATION_KIND" "topic"
            "$SemanticAttributes.MESSAGING_OPERATION" "receive"
            "$SemanticAttributes.MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES" Long
            "$SemanticAttributes.MESSAGING_KAFKA_PARTITION" { it >= 0 }
          }
        }
      }
      trace(1, 1) {
        span(0) {
          name "producer callback"
          kind INTERNAL
          hasNoParent()
        }
      }
    }
  }
}

