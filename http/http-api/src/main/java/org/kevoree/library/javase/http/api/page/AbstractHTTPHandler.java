package org.kevoree.library.javase.http.api.page;

import org.kevoree.annotation.*;
import org.kevoree.api.Context;
import org.kevoree.api.Port;
import org.kevoree.library.javase.http.api.commons.*;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/04/13
 * Time: 10:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
@Library(name = "web")
public abstract class AbstractHTTPHandler {

    @Param(optional = true, defaultValue = "/")
    protected String urlPattern;

    @Param(optional = true, defaultValue = "/")
    protected String patternToRemove;

    @KevoreeInject
    protected Context cmpContext;

    @Output(optional = false)
    protected Port content;

    @Output(optional = true)
    protected Port forward;

    protected static final int NO_RETURN_RESPONSE = 418;

    private KevoreeHttpServlet servlet;

    private final static String DEFAULT_SC_INTERNAL_ERROR_MESSAGE = "<html><body><h1>Internal Error !</h1><b>Please contact the administrator of the server</b></body></html>";

    @Start
    public void start() throws Exception {
        servlet = new KevoreeHttpServlet();
    }

    @Update
    public void update() throws Exception {
            stop();
            start();
    }

    @Stop
    public void stop() throws Exception {
        if (servlet != null) {
            servlet.destroy();
            servlet = null;
        }
    }

    public String applyPatternToRemove(String uri) {
        Pattern pattern = Pattern.compile(patternToRemove);
        Matcher matcher = pattern.matcher(uri);
        return matcher.replaceFirst("");
    }

    public void forward(HttpServletRequest req, HttpServletResponse response) {
        if (req instanceof KevoreeHTTPServletRequest) {
            KevoreeHTTPServletRequestWrapper request = new KevoreeHTTPServletRequestWrapper((KevoreeHTTPServletRequest) req);

            String uri = request.getRequestURI();
            uri = applyPatternToRemove(uri);
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }
            request.setRequestURI(uri);
            if (forward != null) {
                Log.debug("forward request for url = {} with completeURL = {}", uri, request.getRequestURL());
                forward.send(request);
                response.setStatus(NO_RETURN_RESPONSE);
            } else {
                Log.debug("Unable to forward request because the forward port is not bound for {}", cmpContext.getInstanceName());
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to forward request because the forward port is not bound for " + cmpContext.getInstanceName() + "@" + cmpContext.getNodeName());
                } catch (IOException e) {
                    Log.error("Unable to send Internal Server Error to notify that the forward port is not bound");
                }
            }
        }
    }

    private boolean check(String url) {
        Log.trace("Checking url in component '{}' with urlPattern '{}' and url '{}'", cmpContext.getInstanceName(), urlPattern, url);
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher m = pattern.matcher(url);
        return m.matches();
    }

    // TODO change type and name of the parameter
    @Input(optional = false)
    public void request(/*HTTPOperationTuple*/Object msg) {
        if (msg != null && msg instanceof HTTPOperationTuple) {
            HttpServletRequest request = ((HTTPOperationTuple) msg).request;
            if (check(request.getPathInfo())) {
                Log.debug("The url '{}' is accepted by '{}' with urlPattern '{}' ", request.getPathInfo(), cmpContext.getInstanceName(), urlPattern);
                KevoreeHTTPServletResponse response = ((HTTPOperationTuple) msg).response;
                if (request.getMethod().equalsIgnoreCase("get")) {
                    try {
                        doGet(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                } else if (request.getMethod().equalsIgnoreCase("head")) {
                    try {
                        doHead(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                } else if (request.getMethod().equalsIgnoreCase("post")) {
                    try {
                        doPost(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                } else if (request.getMethod().equalsIgnoreCase("put")) {
                    try {
                        doPut(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                } else if (request.getMethod().equalsIgnoreCase("delete")) {
                    try {
                        doDelete(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                } else if (request.getMethod().equalsIgnoreCase("options")) {
                    try {
                        doOptions(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                } else if (request.getMethod().equalsIgnoreCase("trace")) {
                    try {
                        doTrace(request, response);
                    } catch (ServletException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    } catch (IOException e) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Log.info("Unable to fulfill the request due to an exception", e);
                    }
                }
                if (response.getStatus() != 418) {
                    if (response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                        try {
                            PrintWriter writer = response.getWriter();
                            writer.write(DEFAULT_SC_INTERNAL_ERROR_MESSAGE);
                            writer.flush();
                            writer.close();
                        } catch (IOException ignored) {}
                    }
                    content.send(msg);
                } else {
                    Log.debug("Status code correspond to tea pot: No response returns!");
                }
            }
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doGet(req, resp);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doHead(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doPost(req, resp);
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doPut(req, resp);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doDelete(req, resp);
    }

    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doOptions(req, resp);
    }

    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servlet.doTrace(req, resp);
    }
}
