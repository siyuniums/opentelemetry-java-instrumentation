/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.instrumentation.test.AgentTestTrait
import io.opentelemetry.instrumentation.test.asserts.TraceAssert
import io.opentelemetry.instrumentation.test.base.HttpServerTest
import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint
import io.opentelemetry.javaagent.bootstrap.servlet.SnippetHolder
import io.opentelemetry.testing.internal.armeria.common.AggregatedHttpRequest

import javax.servlet.Servlet

import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.AUTH_REQUIRED
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_HEADERS
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_PARAMETERS
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.ERROR
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.EXCEPTION
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.HTML
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.HTML2
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.NOT_FOUND
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.QUERY_PARAM
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.REDIRECT
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.SUCCESS

abstract class AbstractServlet3Test<SERVER, CONTEXT> extends HttpServerTest<SERVER> implements AgentTestTrait {
  @Override
  URI buildAddress() {
    return new URI("http://localhost:$port$contextPath/")
  }

  // FIXME: Add authentication tests back in...
//  @Shared
//  protected String user = "user"
//  @Shared
//  protected String pass = "password"

  abstract Class<Servlet> servlet()

  abstract void addServlet(CONTEXT context, String path, Class<Servlet> servlet)

  protected void setupServlets(CONTEXT context) {
    def servlet = servlet()

    addServlet(context, SUCCESS.path, servlet)
    addServlet(context, QUERY_PARAM.path, servlet)
    addServlet(context, ERROR.path, servlet)
    addServlet(context, EXCEPTION.path, servlet)
    addServlet(context, REDIRECT.path, servlet)
    addServlet(context, AUTH_REQUIRED.path, servlet)
    addServlet(context, INDEXED_CHILD.path, servlet)
    addServlet(context, CAPTURE_HEADERS.path, servlet)
    addServlet(context, CAPTURE_PARAMETERS.path, servlet)
    addServlet(context, HTML.path, servlet)
    addServlet(context, HTML2.path, servlet)
  }

  protected ServerEndpoint lastRequest

  @Override
  AggregatedHttpRequest request(ServerEndpoint uri, String method) {
    lastRequest = uri
    super.request(uri, method)
  }

  @Override
  String expectedHttpRoute(ServerEndpoint endpoint) {
    switch (endpoint) {
      case NOT_FOUND:
        return getContextPath() + "/*"
      default:
        return super.expectedHttpRoute(endpoint)
    }
  }

  @Override
  boolean testCapturedRequestParameters() {
    true
  }

  boolean errorEndpointUsesSendError() {
    true
  }

  @Override
  boolean hasResponseSpan(ServerEndpoint endpoint) {
    endpoint == REDIRECT || (endpoint == ERROR && errorEndpointUsesSendError())
  }

  @Override
  void responseSpan(TraceAssert trace, int index, Object parent, String method, ServerEndpoint endpoint) {
    switch (endpoint) {
      case REDIRECT:
        redirectSpan(trace, index, parent)
        break
      case ERROR:
        sendErrorSpan(trace, index, parent)
        break
    }
  }

  def "snippet injection with ServletOutPutStream"() {
    setup:
    SnippetHolder.setSnippet("\n  <script type=\"text/javascript\"> Test </script>")
    def request = request(HTML2, "GET")
    def response = client.execute(request).aggregate().join()

    expect:
    response.status().code() == HTML2.status
    // check response content-length header
    String result = "<!DOCTYPE html>\n" +
      "<html lang=\"en\">\n" +
      "<head>\n" +
      "  <script type=\"text/javascript\"> Test </script>\n" +
      "  <meta charset=\"UTF-8\">\n" +
      "  <title>Title</title>\n" +
      "</head>\n" +
      "<body>\n" +
      "<p>test works</p>\n" +
      "</body>\n" +
      "</html>"
    response.contentUtf8() == result
    response.headers().contentLength() == result.length();
  }

  def "snippet injection with PrintWriter"() {
    setup:
    SnippetHolder.setSnippet("\n  <script type=\"text/javascript\"> Test </script>")
    def request = request(HTML, "GET")
    def response = client.execute(request).aggregate().join()

    expect:
    response.status().code() == HTML.status
    String result = "<!DOCTYPE html>\n" +
      "<html lang=\"en\">\n" +
      "<head>\n" +
      "  <script type=\"text/javascript\"> Test </script>\n" +
      "  <meta charset=\"UTF-8\">\n" +
      "  <title>Title</title>\n" +
      "</head>\n" +
      "<body>\n" +
      "<p>test works</p>\n" +
      "</body>\n" +
      "</html>"

    response.contentUtf8() == result
    response.headers().contentLength() == result.length();
  }


}
