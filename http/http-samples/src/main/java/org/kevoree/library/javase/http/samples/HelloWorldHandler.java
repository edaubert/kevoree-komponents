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

        StringBuilder builder = new StringBuilder();
        PrintWriter out = resp.getWriter();

        builder.append("<html><head></head><body>");
        builder.append("Hello world !!!");

        builder.append("<br/>");
        builder.append("Hello coming from " + getName() + " on " + getNodeName());
        builder.append("</body></html>");

        out.write(builder.toString());
        out.flush();

        resp.setContentLength(builder.length());
        resp.addDateHeader("Date", System.currentTimeMillis());
        resp.addDateHeader("Last-Modified", lastModified);

    }
}
