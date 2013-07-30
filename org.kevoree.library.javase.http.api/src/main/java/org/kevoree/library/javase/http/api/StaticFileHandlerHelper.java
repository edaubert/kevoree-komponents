package org.kevoree.library.javase.http.api;

import org.kevoree.log.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/07/13
 * Time: 16:41
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class StaticFileHandlerHelper {


    private static Properties mime = initMime();

    protected static Properties initMime() {
        Properties p = new Properties();
        try {
            p.load(StaticFileHandlerHelper.class.getClassLoader().getResourceAsStream("mime.properties"));
        } catch (Exception ex) {
            Log.debug("MIME map can't be loaded:" + ex);
        }
        return p;
    }


    public static boolean checkStaticFileFromDir(String defaultFile, String baseDir, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp) {
        String filePath = req.getRequestURI();
        Log.warn("filePath we are looking for: {}", filePath);
        filePath = origin.applyPatternToRemove(filePath);

        if (filePath.contains("?")) {
            filePath = filePath.substring(0, filePath.indexOf("?"));
        }

        if (filePath == null || "".equals(filePath) || "/".equals(filePath)) {
            filePath = defaultFile;
        }

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        filePath.replace("/", File.separator);
        Log.debug("Request received to get file {}", baseDir + File.separator + filePath);
        File in = new File(baseDir + File.separator + filePath);
        if (in.exists() && in.isFile()) {
            try {
                FileInputStream ins = new FileInputStream(in);

                if (isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(convertStream(ins));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(convertStream(ins), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (getHttpHeaderFromURL(filePath)));

                ins.close();
                return true;
            } catch (Exception e) {
                Log.error("", e);
            }

        } else {
            Log.debug("Resource {} doesn't exist ", baseDir + File.separator + filePath);
        }
        return false;
    }

    public static boolean checkStaticFileFromDir(String filePath, String baseDir, HttpServletResponse resp) {
        filePath.replace("/", File.separator);
        Log.debug("Request received to get file {}", baseDir + File.separator + filePath);
        File in = new File(baseDir + File.separator + filePath);
        if (in.exists() && in.isFile()) {
            try {
                FileInputStream ins = new FileInputStream(in);

                if (isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(convertStream(ins));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(convertStream(ins), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (getHttpHeaderFromURL(filePath)));

                ins.close();
                return true;
            } catch (Exception e) {
                Log.error("", e);
            }

        } else {
            Log.debug("Resource {} doesn't exist ", baseDir + File.separator + filePath);
        }
        return false;
    }


    public static boolean checkStaticFile(String defaultFile, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp) {
        String filePath = req.getRequestURI();
        filePath = origin.applyPatternToRemove(filePath);

        if (filePath.contains("?")) {
            filePath = filePath.substring(0, filePath.indexOf("?"));
        }

        if (filePath == null || "".equals(filePath) || "/".equals(filePath)) {
            filePath = defaultFile;
        }

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        Log.debug("Request received to get file {}", filePath);
        InputStream in = origin.getClass().getClassLoader().getResourceAsStream(filePath);
        if (in != null) {
            try {
                if (isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(convertStream(in));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(convertStream(in), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (getHttpHeaderFromURL(filePath)));
                return true;
            } catch (Exception e) {
                Log.error("", e);
            }
        } else {
            Log.debug("Resource {} doesn't exist ", filePath);
        }
        return false;
    }

    public static boolean checkStaticFile(String filePath, AbstractHTTPHandler origin, HttpServletResponse resp) {
        Log.debug("Request received to get file {}", filePath);
        InputStream in = origin.getClass().getClassLoader().getResourceAsStream(filePath);
        if (in != null) {
            try {
                if (isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(convertStream(in));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(convertStream(in), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (getHttpHeaderFromURL(filePath)));
                return true;
            } catch (Exception e) {
                Log.error("", e);
            }
        } else {
            Log.debug("Resource {} doesn't exist ", filePath);
        }
        return false;
    }

    private static boolean isRaw(String url) {
        Log.debug("look extension file to know if the file '{}' is a raw file", url);
        return !(url.endsWith(".js") || url.endsWith(".html") || url.endsWith(".css") || url.endsWith(".jnlp"));
    }

    private static String getHttpHeaderFromURL(String url) {
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
