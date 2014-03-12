package org.kevoree.library.javase.http.samples.pages.helper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/03/14
 * Time: 09:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class CachedKevoreeHttpServletResponse implements HttpServletResponse {
    HttpServletResponse response;

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    CachedServletOutputStream outputStream;

    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new CachedServletOutputStream();
        }
        outputStream.reset();
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getOutputStream());
    }

    @Override
    public void setCharacterEncoding(String s) {
        response.setCharacterEncoding(s);
    }

    @Override
    public void setContentLength(int i) {
        response.setContentLength(i);
    }

    @Override
    public void setContentType(String s) {
        response.setContentType(s);
    }

    @Override
    public void setBufferSize(int i) {
        response.setBufferSize(i);
    }

    @Override
    public int getBufferSize() {
        return response.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        response.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        response.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return response.isCommitted();
    }

    @Override
    public void reset() {
        response.reset();
    }

    @Override
    public void setLocale(Locale locale) {
        response.setLocale(locale);
    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    public byte[] toByteArray() {
        if (outputStream != null) {
            return outputStream.toByteArray();
        } else {
            return new byte[0];
        }
    }

    @Override
    public void addCookie(Cookie cookie) {
        response.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String s) {
        return response.containsHeader(s);
    }

    @Override
    public String encodeURL(String s) {
        return response.encodeURL(s);
    }

    @Override
    public String encodeRedirectURL(String s) {
        return response.encodeRedirectURL(s);
    }

    @Override
    public String encodeUrl(String s) {
        return response.encodeUrl(s);
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return response.encodeRedirectUrl(s);
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        response.sendError(i, s);
    }

    @Override
    public void sendError(int i) throws IOException {
        response.sendError(i);
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        response.sendRedirect(s);
    }

    @Override
    public void setDateHeader(String s, long l) {
        response.setDateHeader(s, l);
    }

    @Override
    public void addDateHeader(String s, long l) {
        response.addDateHeader(s, l);
    }

    @Override
    public void setHeader(String s, String s2) {
        response.setHeader(s, s2);
    }

    @Override
    public void addHeader(String s, String s2) {
        response.addHeader(s, s2);
    }

    @Override
    public void setIntHeader(String s, int i) {
        response.setIntHeader(s, i);
    }

    @Override
    public void addIntHeader(String s, int i) {
        response.addIntHeader(s, i);
    }

    @Override
    public void setStatus(int i) {
        response.setStatus(i);
    }

    @Override
    public void setStatus(int i, String s) {
        response.setStatus(i, s);
    }

    class CachedServletOutputStream extends ServletOutputStream {

        ByteArrayOutputStream outputStream;

        CachedServletOutputStream() {
            this.outputStream = new ByteArrayOutputStream();
        }

        void reset() {
            outputStream.reset();
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
            response.getOutputStream().write(b);
        }

        byte[] toByteArray() {
            return outputStream.toByteArray();
        }
    }

}
