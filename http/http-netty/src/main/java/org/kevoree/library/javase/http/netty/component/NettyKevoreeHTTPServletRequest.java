package org.kevoree.library.javase.http.netty.component;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.kevoree.library.javase.http.api.page.KevoreeHTTPServletRequest;
import org.kevoree.library.javase.http.netty.helpers.Reader;

import java.io.IOException;
import java.util.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/04/13
 * Time: 10:44
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyKevoreeHTTPServletRequest extends KevoreeHTTPServletRequest {

    private FullHttpRequest httpRequest;
    private NettyHTTPServer server;

    private Map<String, String[]> parameters;

    public NettyKevoreeHTTPServletRequest(FullHttpRequest httpRequest, NettyHTTPServer server) {
        this.httpRequest = httpRequest;
        this.server = server;
        try {
            String content = new String(Reader.readContent(httpRequest.content()), "UTF-8");
            QueryStringDecoder decoder = new QueryStringDecoder(content, false);
            parameters = new HashMap<String, String[]>(decoder.parameters().size());
            for (String name : decoder.parameters().keySet()) {
                String[] values = new String[decoder.parameters().get(name).size()];
                decoder.parameters().get(name).toArray(values);
                parameters.put(name, values);
            }
            parameters = Collections.unmodifiableMap(parameters);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return httpRequest.headers().get(name);
    }

    @Override
    public Enumeration getHeaders(String name) {
        return Collections.enumeration(httpRequest.headers().getAll(name));
    }

    @Override
    public Enumeration getHeaderNames() {
        return Collections.enumeration(httpRequest.headers().names());
    }

    @Override
    public int getIntHeader(String name) {
        return Integer.parseInt(httpRequest.headers().get(name));
    }

    @Override
    public String getMethod() {
        return httpRequest.getMethod().name();
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
        return httpRequest.getUri();
    }

    @Override
    public StringBuffer getRequestURL() {
//        return new StringBuffer(server.getUri().toASCIIString());
        // FIXME
//        httpRequest.
        return new StringBuffer();
    }

    @Override
    public Locale getLocale() {
        HttpHeaders headers = httpRequest.headers();
        if (headers.contains("Accept-Language")) {
            return Locale.forLanguageTag(headers.get("Accept-Language"));
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
        String[] cookies = httpRequest.headers().get("Cookie").split(";");
        for (String cookie : cookies) {
            if (cookie.toLowerCase().startsWith("sessionid")) {
                return cookie.substring(("sessionid").length()).replaceFirst("=", "").trim();
            }
        }
        return null;
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
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name)[0];
    }
}
