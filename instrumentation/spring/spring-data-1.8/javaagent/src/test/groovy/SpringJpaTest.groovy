/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.instrumentation.test.AgentInstrumentationSpecification
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import org.hibernate.Version
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import spring.jpa.JpaCustomer
import spring.jpa.JpaCustomerRepository
import spring.jpa.JpaPersistenceConfig

import static io.opentelemetry.api.trace.SpanKind.CLIENT
import static io.opentelemetry.api.trace.SpanKind.INTERNAL

class SpringJpaTest extends AgentInstrumentationSpecification {
  def "test object method"() {
    setup:
    def context = new AnnotationConfigApplicationContext(JpaPersistenceConfig)
    def repo = context.getBean(JpaCustomerRepository)

    // when Spring JPA sets up, it issues metadata queries -- clear those traces
    clearExportedData()

    when:
    runWithSpan("toString test") {
      repo.toString()
    }

    then:
    // Asserting that a span is NOT created for toString
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          name "toString test"
          attributes {
          }
        }
      }
    }
  }

  def "test CRUD"() {
    def isHibernate4 = Version.getVersionString().startsWith("4.")
    // moved inside test -- otherwise, miss the opportunity to instrument
    def context = new AnnotationConfigApplicationContext(JpaPersistenceConfig)
    def repo = context.getBean(JpaCustomerRepository)

    // when Spring JPA sets up, it issues metadata queries -- clear those traces
    clearExportedData()

    setup:
    def customer = new JpaCustomer("Bob", "Anonymous")

    expect:
    customer.id == null
    !repo.findAll().iterator().hasNext() // select

    assertTraces(1) {
      trace(0, 2) {
        span(0) {
          name "JpaCustomerRepository.findAll"
          kind INTERNAL
          attributes {
            "$SemanticAttributes.CODE_NAMESPACE" JpaCustomerRepository.name
            "$SemanticAttributes.CODE_FUNCTION" "findAll"
          }
        }
        span(1) { // select
          name "SELECT test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^select /
            "$SemanticAttributes.DB_OPERATION" "SELECT"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
      }
    }
    clearExportedData()

    when:
    repo.save(customer) // insert
    def savedId = customer.id

    then:
    customer.id != null
    assertTraces(1) {
      trace(0, 2 + (isHibernate4 ? 0 : 1)) {
        span(0) {
          name "JpaCustomerRepository.save"
          kind INTERNAL
          attributes {
            "$SemanticAttributes.CODE_NAMESPACE" JpaCustomerRepository.name
            "$SemanticAttributes.CODE_FUNCTION" "save"
          }
        }
        def offset = 0
        // hibernate5 has extra span
        if (!isHibernate4) {
          offset = 1
          span(1) {
            name "test"
            kind CLIENT
            childOf span(0)
            attributes {
              "$SemanticAttributes.DB_SYSTEM" "hsqldb"
              "$SemanticAttributes.DB_NAME" "test"
              "$SemanticAttributes.DB_USER" "sa"
              "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
              "$SemanticAttributes.DB_STATEMENT" "call next value for hibernate_sequence"
            }
          }
        }
        span(1 + offset) { // insert
          name "INSERT test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^insert /
            "$SemanticAttributes.DB_OPERATION" "INSERT"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
      }
    }
    clearExportedData()

    when:
    customer.firstName = "Bill"
    repo.save(customer)

    then:
    customer.id == savedId
    assertTraces(1) {
      trace(0, 3) {
        span(0) {
          name "JpaCustomerRepository.save"
          kind INTERNAL
          attributes {
            "$SemanticAttributes.CODE_NAMESPACE" JpaCustomerRepository.name
            "$SemanticAttributes.CODE_FUNCTION" "save"
          }
        }
        span(1) { // select
          name "SELECT test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^select /
            "$SemanticAttributes.DB_OPERATION" "SELECT"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
        span(2) { // update
          name "UPDATE test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^update /
            "$SemanticAttributes.DB_OPERATION" "UPDATE"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
      }
    }
    clearExportedData()

    when:
    customer = repo.findByLastName("Anonymous")[0] // select

    then:
    assertTraces(1) {
      trace(0, 2) {
        span(0) {
          name "JpaCustomerRepository.findByLastName"
          kind INTERNAL
          attributes {
            "$SemanticAttributes.CODE_NAMESPACE" JpaCustomerRepository.name
            "$SemanticAttributes.CODE_FUNCTION" "findByLastName"
          }
        }
        span(1) { // select
          name "SELECT test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^select /
            "$SemanticAttributes.DB_OPERATION" "SELECT"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
      }
    }
    clearExportedData()

    when:
    repo.delete(customer) // delete

    then:
    assertTraces(1) {
      trace(0, 3) {
        span(0) {
          name "JpaCustomerRepository.delete"
          kind INTERNAL
          attributes {
            "$SemanticAttributes.CODE_NAMESPACE" JpaCustomerRepository.name
            "$SemanticAttributes.CODE_FUNCTION" "delete"
          }
        }
        span(1) { // select
          name "SELECT test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^select /
            "$SemanticAttributes.DB_OPERATION" "SELECT"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
        span(2) { // delete
          name "DELETE test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^delete /
            "$SemanticAttributes.DB_OPERATION" "DELETE"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
      }
    }
  }

  def "test custom repository method"() {
    def context = new AnnotationConfigApplicationContext(JpaPersistenceConfig)
    def repo = context.getBean(JpaCustomerRepository)

    // when Spring JPA sets up, it issues metadata queries -- clear those traces
    clearExportedData()

    setup:
    def customers = repo.findSpecialCustomers()

    expect:
    customers.isEmpty()

    assertTraces(1) {
      trace(0, 2) {
        span(0) {
          name "JpaCustomerRepository.findSpecialCustomers"
          kind INTERNAL
          attributes {
            "$SemanticAttributes.CODE_NAMESPACE" JpaCustomerRepository.name
            "$SemanticAttributes.CODE_FUNCTION" "findSpecialCustomers"
          }
        }
        span(1) { // select
          name "SELECT test.JpaCustomer"
          kind CLIENT
          childOf span(0)
          attributes {
            "$SemanticAttributes.DB_SYSTEM" "hsqldb"
            "$SemanticAttributes.DB_NAME" "test"
            "$SemanticAttributes.DB_USER" "sa"
            "$SemanticAttributes.DB_CONNECTION_STRING" "hsqldb:mem:"
            "$SemanticAttributes.DB_STATEMENT" ~/^select /
            "$SemanticAttributes.DB_OPERATION" "SELECT"
            "$SemanticAttributes.DB_SQL_TABLE" "JpaCustomer"
          }
        }
      }
    }
  }
}
