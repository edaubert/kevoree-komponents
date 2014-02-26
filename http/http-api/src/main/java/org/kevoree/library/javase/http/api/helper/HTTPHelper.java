package org.kevoree.library.javase.http.api.helper;

import org.kevoree.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    /**
     *
     * @param url the URL from which we want to know if the response contains raw data
     *            Non RAW data is <code>js, html, css and jnlp</code> file
     * @return true if the content will be raw data, false otherwise
     */
    public static boolean isRaw(String url) {
        Log.debug("look extension file to know if the file '{}' is a raw file", url);
        return !(url.endsWith(".js") || url.endsWith(".html") || url.endsWith(".css") || url.endsWith(".jnlp"));
    }

    /**
     *
     * @param url the URL from which we want to find the MIME-TYPE
     * @return a MIME-TYPE coming a predefined ones
     */
    public static String getMimeTypeFromURL(String url) {
        int dp = url.lastIndexOf('.');
        if (dp > 0) {
            return mime.getProperty(url.substring(dp + 1).toUpperCase());
        }
        return "text/html";
    }

    /**
     *
     * @param in the {@link java.io.InputStream} from the content is read
     * @param out the {@link java.io.OutputStream} where the content is written
     * @return the length of the data written on <code>out</code>
     * @throws IOException if read or write goes wrong
     */
    public static int convertStream(InputStream in, OutputStream out) throws IOException {
        int total = 0;
        byte[] buffer = new byte[1024];
        int l;
        do {
            l = (in.read(buffer));
            if (l > 0) {
                out.write(buffer, 0, l);
            }
            total += l;
        } while (l > 0);
        return total;
    }
}
