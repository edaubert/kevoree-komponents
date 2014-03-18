package org.kevoree.library.javase.http.netty.channel;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.kevoree.library.javase.http.netty.NettyClientHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/03/14
 * Time: 15:49
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyDataClientHandler extends NettyClientHandler {

    @Override
    protected void processRequest(String host, int port, String uri, InputStream content, FullHttpRequest request) throws IOException {
        byte[] bytes = new byte[2048];
        long size = 0;
        int length = content.read(bytes);
        while (length != -1) {
            request.content().writeBytes(bytes, 0, length);
            size += length;
            length = content.read(bytes);
        }

        HttpHeaders headers = request.headers();

        headers.set(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED);
        headers.set(HttpHeaders.Names.CONTENT_LENGTH, size);
    }
}
