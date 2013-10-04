package org.kevoree.library.javase.http.api;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
@Library(name = "web")
@ComponentType
@Provides({
        @ProvidedPort(name = "request", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "content", type = PortType.MESSAGE),
        @RequiredPort(name = "forward", type = PortType.MESSAGE, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "urlPattern", optional = true, defaultValue = "/"),
        @DictionaryAttribute(name = "patternToRemove", optional = true, defaultValue = "/")
})
public abstract class AbstractHTTPHandler extends AbstractComponentType {
    protected static final int NO_RETURN_RESPONSE = 418;

    private KevoreeHttpServlet servlet;
    protected String urlPatternRegex;
    protected String patternToRemove;

    @Start
    public void start() throws Exception {
        servlet = new KevoreeHttpServlet();
        urlPatternRegex = getDictionary().get("urlPattern").toString();
        patternToRemove = getDictionary().get("patternToRemove").toString();
    }

    @Update
    public void update() throws Exception {
        if (!urlPatternRegex.equals(getDictionary().get("urlPattern").toString()) && !patternToRemove.equals(getDictionary().get("patternToRemove").toString())) {
            stop();
            start();
        }
    }

    @Stop
    public void stop() throws Exception {
        if (servlet != null) {
            servlet.destroy();
            servlet = null;
        }
    }

    protected String applyPatternToRemove(String uri) {
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
            if (isPortBinded("forward")) {
                Log.debug("forward request for url = {} with completeURL = {}", uri, request.getRequestURL());
                getPortByName("forward", MessagePort.class).process(request);
                response.setStatus(NO_RETURN_RESPONSE);
            } else {
                Log.debug("Unable to forward request because the forward port is not bound for {}", getName());
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to forward request because the forward port is not bound for " + getName() + "@" + getNodeName());
                } catch (IOException e) {
                    Log.error("Unable to send Internal Server Error to notify that the forward port is not bound");
                }
            }
        }
    }

    private boolean check(String url) {
        Log.trace("Checking url in component '{}' with urlPattern '{}' and url '{}'", getName(), urlPatternRegex, url);
        Pattern pattern = Pattern.compile(urlPatternRegex);
        Matcher m = pattern.matcher(url);
        return m.matches();
    }

    // TODO change type and name of the parameter
    @Port(name = "request")
    public void request(/*HTTPOperationTuple*/Object msg) {
        if (msg != null && msg instanceof HTTPOperationTuple) {
            HttpServletRequest request = ((HTTPOperationTuple) msg).request;
            if (check(request.getPathInfo())) {
                Log.debug("The url '{}' is accepted by '{}' with urlPattern '{}' ", request.getPathInfo(), getName(), urlPatternRegex);
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
                    ((HTTPOperationTuple) msg).response = response;
                    this.getPortByName("content", MessagePort.class).process(msg);//SEND MESSAGE
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
