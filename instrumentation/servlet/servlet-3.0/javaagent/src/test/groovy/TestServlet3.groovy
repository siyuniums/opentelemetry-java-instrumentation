/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.instrumentation.test.base.HttpServerTest
import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint

import javax.servlet.RequestDispatcher
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.CountDownLatch

import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_HEADERS
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.CAPTURE_PARAMETERS
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.ERROR
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.EXCEPTION
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.HTML
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.HTML2
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.QUERY_PARAM
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.REDIRECT
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.SUCCESS

class TestServlet3 {

  @WebServlet
  static class Sync extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
      String servletPath = req.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH)
      if (servletPath == null) {
        servletPath = req.servletPath
      }
      ServerEndpoint endpoint = ServerEndpoint.forPath(servletPath)
      HttpServerTest.controller(endpoint) {
        resp.contentType = "text/plain"
        switch (endpoint) {
          case SUCCESS:
            resp.status = endpoint.status
            resp.writer.print(endpoint.body)
            break
          case INDEXED_CHILD:
            resp.status = endpoint.status
            endpoint.collectSpanAttributes { req.getParameter(it) }
            break
          case QUERY_PARAM:
            resp.status = endpoint.status
            resp.writer.print(req.queryString)
            break
          case REDIRECT:
            resp.sendRedirect(endpoint.body)
            break
          case CAPTURE_HEADERS:
            resp.setHeader("X-Test-Response", req.getHeader("X-Test-Request"))
            resp.status = endpoint.status
            resp.writer.print(endpoint.body)
            break
          case CAPTURE_PARAMETERS:
            req.setCharacterEncoding("UTF8")
            def value = req.getParameter("test-parameter")
            if (value != "test value õäöü") {
              throw new ServletException("request parameter does not have expected value " + value)
            }
            resp.status = endpoint.status
            resp.writer.print(endpoint.body)
            break
          case ERROR:
            resp.sendError(endpoint.status, endpoint.body)
            break
          case EXCEPTION:
            throw new ServletException(endpoint.body)
          case HTML:
            resp.contentType = "text/html"
            resp.status = endpoint.status
            resp.setContentLength(endpoint.body.length())
            resp.writer.print(endpoint.body)
            break
          case HTML2:
            resp.contentType = "text/html"
            resp.status = endpoint.status
            try {
              resp.setContentLengthLong(endpoint.body.length())
            } catch (Exception e) {
              // servlet 3.0
              resp.setContentLength(endpoint.body.length())
            }
            byte[] body = endpoint.body.getBytes()
            resp.getOutputStream().write(body, 0, body.length)
            break
        }
      }
    }
  }

  @WebServlet(asyncSupported = true)
  static class Async extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
      ServerEndpoint endpoint = ServerEndpoint.forPath(req.servletPath)
      def latch = new CountDownLatch(1)
      def context = req.startAsync()
      if (endpoint == EXCEPTION) {
        context.setTimeout(5000)
      }
      context.start {
        try {
          HttpServerTest.controller(endpoint) {
            resp.contentType = "text/plain"
            switch (endpoint) {
              case SUCCESS:
                resp.status = endpoint.status
                resp.writer.print(endpoint.body)
                context.complete()
                break
              case INDEXED_CHILD:
                endpoint.collectSpanAttributes { req.getParameter(it) }
                resp.status = endpoint.status
                context.complete()
                break
              case QUERY_PARAM:
                resp.status = endpoint.status
                resp.writer.print(req.queryString)
                context.complete()
                break
              case REDIRECT:
                resp.sendRedirect(endpoint.body)
                context.complete()
                break
              case CAPTURE_HEADERS:
                resp.setHeader("X-Test-Response", req.getHeader("X-Test-Request"))
                resp.status = endpoint.status
                resp.writer.print(endpoint.body)
                context.complete()
                break
              case CAPTURE_PARAMETERS:
                req.setCharacterEncoding("UTF8")
                def value = req.getParameter("test-parameter")
                if (value != "test value õäöü") {
                  throw new ServletException("request parameter does not have expected value " + value)
                }
                resp.status = endpoint.status
                resp.writer.print(endpoint.body)
                context.complete()
                break
              case ERROR:
                resp.status = endpoint.status
                resp.writer.print(endpoint.body)
//                resp.sendError(endpoint.status, endpoint.body)
                context.complete()
                break
              case EXCEPTION:
                resp.status = endpoint.status
                def writer = resp.writer
                writer.print(endpoint.body)
                if (req.getClass().getName().contains("catalina")) {
                  // on tomcat close the writer to ensure response is sent immediately, otherwise
                  // there is a chance that tomcat resets the connection before the response is sent
                  writer.close()
                }
                throw new ServletException(endpoint.body)
                break
              case HTML:
                resp.contentType = "text/html"
                resp.status = endpoint.status
                resp.writer.print(endpoint.body)
                resp.setContentLength(endpoint.body.length())
                context.complete()
                break
              case HTML2:
                resp.contentType = "text/html"
                resp.status = endpoint.status
                resp.setContentLengthLong(endpoint.body.length())
                resp.getOutputStream().print(endpoint.body)
                context.complete()
                break
            }
          }
        } finally {
          latch.countDown()
        }
      }
      latch.await()
    }
  }

  @WebServlet(asyncSupported = true)
  static class FakeAsync extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
      def context = req.startAsync()
      try {
        ServerEndpoint endpoint = ServerEndpoint.forPath(req.servletPath)
        HttpServerTest.controller(endpoint) {
          resp.contentType = "text/plain"
          switch (endpoint) {
            case SUCCESS:
              resp.status = endpoint.status
              resp.writer.print(endpoint.body)
              break
            case INDEXED_CHILD:
              endpoint.collectSpanAttributes { req.getParameter(it) }
              resp.status = endpoint.status
              break
            case QUERY_PARAM:
              resp.status = endpoint.status
              resp.writer.print(req.queryString)
              break
            case REDIRECT:
              resp.sendRedirect(endpoint.body)
              break
            case CAPTURE_HEADERS:
              resp.setHeader("X-Test-Response", req.getHeader("X-Test-Request"))
              resp.status = endpoint.status
              resp.writer.print(endpoint.body)
              break
            case CAPTURE_PARAMETERS:
              req.setCharacterEncoding("UTF8")
              def value = req.getParameter("test-parameter")
              if (value != "test value õäöü") {
                throw new ServletException("request parameter does not have expected value " + value)
              }
              resp.status = endpoint.status
              resp.writer.print(endpoint.body)
              break
            case ERROR:
              resp.sendError(endpoint.status, endpoint.body)
              break
            case EXCEPTION:
              resp.status = endpoint.status
              resp.writer.print(endpoint.body)
              throw new ServletException(endpoint.body)
            case HTML:
              resp.status = endpoint.status
              resp.contentType = "text/html"
              resp.writer.print(endpoint.body)
              break
            case HTML2:
              resp.contentType = "text/html"
              resp.status = endpoint.status
              resp.getOutputStream().print(endpoint.body)
              break
          }
        }
      } finally {
        context.complete()
      }
    }
  }

  @WebServlet(asyncSupported = true)
  static class DispatchImmediate extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
      def target = req.servletPath.replace("/dispatch", "")
      if (req.queryString != null) {
        target += "?" + req.queryString
      }
      req.startAsync().dispatch(target)
    }
  }

  @WebServlet(asyncSupported = true)
  static class DispatchAsync extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
      def target = req.servletPath.replace("/dispatch", "")
      if (req.queryString != null) {
        target += "?" + req.queryString
      }
      def context = req.startAsync()
      context.start {
        context.dispatch(target)
      }
    }
  }

  // TODO: Add tests for this!
  @WebServlet(asyncSupported = true)
  static class DispatchRecursive extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
      if (req.servletPath.equals("/recursive")) {
        resp.writer.print("Hello Recursive")
        return
      }
      def depth = Integer.parseInt(req.getParameter("depth"))
      if (depth > 0) {
        req.startAsync().dispatch("/dispatch/recursive?depth=" + (depth - 1))
      } else {
        req.startAsync().dispatch("/recursive")
      }
    }
  }
}
