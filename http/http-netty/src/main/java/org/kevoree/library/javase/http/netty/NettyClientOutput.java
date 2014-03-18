package org.kevoree.library.javase.http.netty;

import java.io.InputStream;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/03/14
 * Time: 13:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyClientOutput {
    private int responseCode;
    private InputStream content;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }
}
