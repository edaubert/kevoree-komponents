package org.kevoree.library.javase.http.netty;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.kevoree.library.javase.http.api.KevoreeHTTPServletResponse;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/04/13
 * Time: 11:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyKevoteeHTTPServletResponse extends KevoreeHTTPServletResponse {

    private FullHttpResponse httpResponse;
    private NettyKevoreeServletOutputStream outputStream;

    private class NettyKevoreeServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream stream;
        FullHttpResponse httpResponse;

        private ByteArrayOutputStream getStream() {
            return stream;
        }

        private NettyKevoreeServletOutputStream(FullHttpResponse httpResponse) {
            stream = new ByteArrayOutputStream();
            this.httpResponse = httpResponse;
        }

        @Override
        public void write(int b) throws IOException {
            httpResponse.content().writeByte(b);
        }
    }

    public NettyKevoteeHTTPServletResponse(FullHttpResponse httpResponse) {
        this.httpResponse = httpResponse;
        outputStream = new NettyKevoreeServletOutputStream(httpResponse);
    }

    @Override
    public int getStatus() {
        return httpResponse.getStatus().code();
    }

    @Override
    public boolean containsHeader(String name) {
        return httpResponse.headers().contains(name);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        httpResponse.setStatus(new HttpResponseStatus(sc, msg));
    }

    @Override
    public void sendError(int sc) throws IOException {
        httpResponse.setStatus(new HttpResponseStatus(sc, ""));
    }

    @Override
    public void setDateHeader(String name, long date) {
        httpResponse.headers().remove(name);
        addDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        httpResponse.headers().add(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        httpResponse.headers().remove(name);
        addHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        httpResponse.headers().add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        httpResponse.headers().remove(name);
        addIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        httpResponse.headers().add(name, value);
    }

    @Override
    public void setStatus(int sc) {
        httpResponse.setStatus(new HttpResponseStatus(sc, ""));
    }

    @Override
    public String getCharacterEncoding() {
        return httpResponse.headers().get("Content-Encoding");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getOutputStream());
    }

    @Override
    public void setCharacterEncoding(String charset) {
        httpResponse.headers().remove("Content-Encoding");
        httpResponse.headers().add("Content-Encoding", charset);
    }

    @Override
    public void setContentLength(int len) {
        httpResponse.headers().remove("Content-Length");
        httpResponse.headers().add("Content-Length", len);
    }

    @Override
    public void setContentType(String type) {
        httpResponse.headers().remove("Content-Type");
        httpResponse.headers().add("Content-Type", type);
    }
}
