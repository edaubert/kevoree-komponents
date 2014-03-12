package org.kevoree.library.javase.http.webbit;

import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletRequest;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebServer;

import java.util.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/04/13
 * Time: 10:44
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class WebbitKevoreeHTTPServletRequest extends KevoreeHTTPServletRequest {

    private HttpRequest httpRequest;
    private WebServer server;

    private Map<String, String[]> parameters;

    public WebbitKevoreeHTTPServletRequest(HttpRequest httpRequest, WebServer server) {
        this.httpRequest = httpRequest;
        this.server = server;
        parameters = new HashMap<String, String[]>(httpRequest.postParamKeys().size());
        for (String name : httpRequest.postParamKeys()) {
            parameters.put(name, getParameterValues(name));
        }
        parameters = Collections.unmodifiableMap(parameters);
    }

    @Override
    public int getLocalPort() {
        return server.getPort();
    }

    @Override
    public long getDateHeader(String name) {
        return super.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return httpRequest.header(name);
    }

    @Override
    public Enumeration getHeaders(String name) {
        return Collections.enumeration(httpRequest.headers(name));
    }

    @Override
    public Enumeration getHeaderNames() {
        List<Map.Entry<String, String>> headers = httpRequest.allHeaders();
        List<String> headerNames = new ArrayList<String>(headers.size());
        for (Map.Entry<String, String> header : headers) {
            headerNames.add(header.getKey());
        }
        return Collections.enumeration(headerNames);
    }

    @Override
    public int getIntHeader(String name) {
        return Integer.parseInt(httpRequest.header(name));
    }

    @Override
    public String getMethod() {
        return httpRequest.method();
    }

    @Override
    public String getPathInfo() {
        String uri = getRequestURI();
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.lastIndexOf("?"));
        }
        return uri;
    }

    @Override
    public String getQueryString() {
        String uri = getRequestURI();
        if (uri.contains("?")) {
            return uri.substring(uri.lastIndexOf("?"));
        } else {
            return null;
        }
    }

    @Override
    public String getRequestURI() {
        return httpRequest.uri();
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(server.getUri().toASCIIString());
    }

    @Override
    public Locale getLocale() {
        if (httpRequest.hasHeader("Accept-Language")) {
            return Locale.forLanguageTag(httpRequest.header("Accept-Language"));
        } else {
            return Locale.getDefault();
        }
    }

    @Override
    public Enumeration getLocales() {
        return Collections.enumeration(Arrays.asList(Locale.getAvailableLocales()));
    }

    @Override
    public String getRequestedSessionId() {
        if (httpRequest.cookie("sessionid") != null) {
            return httpRequest.cookie("sessionid").getValue();
        } else {
            return null;
        }
    }


    @Override
    public Map getParameterMap() {
        return parameters;
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(httpRequest.postParamKeys());
    }

    @Override
    public String getParameter(String name) {
        return httpRequest.postParam(name);
    }
}
