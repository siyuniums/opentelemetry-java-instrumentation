/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.HTML;
import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.HTML2;

import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcherServlet {
  /* There's something about the getRequestDispatcher call that breaks horribly when these classes
   * are written in groovy.
   */

  @WebServlet(asyncSupported = true)
  public static class Forward extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      String target = req.getServletPath().replace("/dispatch", "");
      ServletContext context = getServletContext();
      RequestDispatcher dispatcher = context.getRequestDispatcher(target);
      dispatcher.forward(req, resp);
    }
  }

  @WebServlet(asyncSupported = true)
  public static class Include extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      String target = req.getServletPath().replace("/dispatch", "");
      ServletContext context = getServletContext();
      RequestDispatcher dispatcher = context.getRequestDispatcher(target);
      // for HTML test case, set content type before calling include as
      // "include" reject modify on resp later
      // check https://statics.teams.cdn.office.net/evergreen-assets/safelinks/1/atp-safelinks.html
      if (ServerEndpoint.forPath(target) == HTML || ServerEndpoint.forPath(target) == HTML2) {
        resp.setContentType("text/html");
      }
      dispatcher.include(req, resp);
    }
  }
}
