package org.kevoree.library.javase.http.netty.helpers;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 11/03/14
 * Time: 16:35
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class Reader {
    public static byte[] readContent(ByteBuf buffer) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (buffer.isReadable()) {
            int length = buffer.readableBytes();
            buffer.readBytes(outputStream, length);
        }
        return outputStream.toByteArray();
    }
}
