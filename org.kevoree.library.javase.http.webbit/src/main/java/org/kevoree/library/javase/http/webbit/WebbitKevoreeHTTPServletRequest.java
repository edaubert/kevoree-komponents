package org.kevoree.library.javase.http.webbit;

import org.kevoree.library.javase.http.api.KevoreeHTTPServletRequest;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebServer;

import javax.servlet.ServletInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public WebbitKevoreeHTTPServletRequest(HttpRequest httpRequest, WebServer server) {
        this.httpRequest = httpRequest;
        this.server = server;
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
    public String getQueryString() {
        return httpRequest.uri().substring(httpRequest.uri().lastIndexOf("?"));
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
    public Object getAttribute(String name) {
        return super.getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return super.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return super.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        super.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        return super.getContentLength();
    }

    @Override
    public String getContentType() {
        return super.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return super.getInputStream();
    }

    @Override
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override
    public Enumeration getParameterNames() {
        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return super.getParameterValues(name);
    }

    @Override
    public Map getParameterMap() {
        return super.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return super.getProtocol();
    }

    @Override
    public String getScheme() {
        return super.getScheme();
    }

    @Override
    public String getServerName() {
        return super.getServerName();
    }

    @Override
    public int getServerPort() {
        return super.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return super.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return super.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return super.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
        super.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        super.removeAttribute(name);
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
    public boolean isSecure() {
        return super.isSecure();
    }

    @Override
    public String getLocalName() {
        return super.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return super.getLocalAddr();
    }
}
