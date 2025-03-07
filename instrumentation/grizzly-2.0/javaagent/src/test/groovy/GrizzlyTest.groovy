/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.instrumentation.test.AgentTestTrait
import io.opentelemetry.instrumentation.test.base.HttpServerTest
import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes
import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.Request
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig

import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper

import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.ERROR
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.EXCEPTION
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.QUERY_PARAM
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.REDIRECT
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.SUCCESS

class GrizzlyTest extends HttpServerTest<HttpServer> implements AgentTestTrait {

  @Override
  HttpServer startServer(int port) {
    ResourceConfig rc = new ResourceConfig()
    rc.register(SimpleExceptionMapper)
    rc.register(ServiceResource)

    def server = GrizzlyHttpServerFactory.createHttpServer(new URI("http://localhost:$port"), rc, false)
    // jersey doesn't propagate exceptions up to the grizzly handler
    // so we use a standalone HttpHandler to test exception capture
    server.getServerConfiguration().addHttpHandler(new ExceptionHttpHandler(), "/exception")
    server.start()

    return server
  }

  @Override
  Set<AttributeKey<?>> httpAttributes(ServerEndpoint endpoint) {
    def attributes = super.httpAttributes(endpoint)
    attributes.remove(SemanticAttributes.HTTP_ROUTE)
    attributes.remove(SemanticAttributes.NET_TRANSPORT)
    attributes
  }

  @Override
  void stopServer(HttpServer server) {
    server.stop()
  }

  @Override
  boolean testCapturedHttpHeaders() {
    false
  }

  static class SimpleExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    Response toResponse(Throwable exception) {
      if (exception instanceof NotFoundException) {
        return exception.getResponse()
      }
      Response.status(500).entity(exception.message).build()
    }
  }

  @Path("/")
  static class ServiceResource {

    @GET
    @Path("success")
    Response success() {
      controller(SUCCESS) {
        Response.status(SUCCESS.status).entity(SUCCESS.body).build()
      }
    }

    @GET
    @Path("query")
    Response query_param(@QueryParam("some") String param) {
      controller(QUERY_PARAM) {
        Response.status(QUERY_PARAM.status).entity("some=$param".toString()).build()
      }
    }

    @GET
    @Path("redirect")
    Response redirect() {
      controller(REDIRECT) {
        Response.status(REDIRECT.status).location(new URI(REDIRECT.body)).build()
      }
    }

    @GET
    @Path("error-status")
    Response error() {
      controller(ERROR) {
        Response.status(ERROR.status).entity(ERROR.body).build()
      }
    }

    @GET
    @Path("child")
    Response exception(@QueryParam("id") String id) {
      controller(INDEXED_CHILD) {
        INDEXED_CHILD.collectSpanAttributes { it == "id" ? id : null }
        Response.status(INDEXED_CHILD.status).entity(INDEXED_CHILD.body).build()
      }
    }
  }

  static class ExceptionHttpHandler extends HttpHandler {

    @Override
    void service(Request request, org.glassfish.grizzly.http.server.Response response) throws Exception {
      controller(EXCEPTION) {
        throw new Exception(EXCEPTION.body)
      }
    }
  }
}
