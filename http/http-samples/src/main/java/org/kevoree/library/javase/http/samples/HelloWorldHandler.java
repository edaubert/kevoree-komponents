package org.kevoree.library.javase.http.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.http.api.AbstractHTTPHandler;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/04/13
 * Time: 11:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class HelloWorldHandler extends AbstractHTTPHandler {

    private long lastModified;

    public HelloWorldHandler() {
        lastModified = System.currentTimeMillis();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.debug("doGet in {} for {}", getName(), req.getRequestURI());
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();

        out.write("<html><head></head><body>");
        out.write("Hello world !!!");

        out.flush();

        out.println("<br/>");
        out.println("Hello coming from " + getName() + " on " + getNodeName());
        out.println("</body></html>");

        out.flush();

        resp.addDateHeader("Date", System.currentTimeMillis());
        resp.addDateHeader("Last-Modified", lastModified);

    }
}
