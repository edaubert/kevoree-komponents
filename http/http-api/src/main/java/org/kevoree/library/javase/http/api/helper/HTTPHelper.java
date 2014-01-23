package org.kevoree.library.javase.http.api.helper;

import org.kevoree.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 31/07/13
 * Time: 17:36
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class HTTPHelper {
    private static Properties mime = initMime();

    protected static Properties initMime() {
        Properties p = new Properties();
        try {
            p.load(StaticFileHandlerHelper.class.getClassLoader().getResourceAsStream("mime.properties"));
        } catch (Exception ex) {
            Log.debug("MIME map can't be loaded: {}", ex, ex.getMessage());
        }
        return p;
    }

    public static boolean isRaw(String url) {
        Log.debug("look extension file to know if the file '{}' is a raw file", url);
        return !(url.endsWith(".js") || url.endsWith(".html") || url.endsWith(".css") || url.endsWith(".jnlp"));
    }

    public static String getHttpHeaderFromURL(String url) {
        int dp = url.lastIndexOf('.');
        if (dp > 0) {
            return mime.getProperty(url.substring(dp + 1).toUpperCase());
        }
        return "text/html";
    }

    public static byte[] convertStream(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int l;
        do {
            l = (in.read(buffer));
            if (l > 0) {
                out.write(buffer, 0, l);
            }
        } while (l > 0);
        return out.toByteArray();
    }
}
