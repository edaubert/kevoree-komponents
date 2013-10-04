package org.kevoree.library.javase.http.api;

import org.kevoree.log.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/07/13
 * Time: 16:41
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class StaticFileHandlerHelper {

    public static boolean checkStaticFileFromDir(String defaultFile, String baseDir, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp) {
        String filePath = req.getPathInfo();
        filePath = origin.applyPatternToRemove(filePath);

        if (filePath.contains("?")) {
            filePath = filePath.substring(0, filePath.indexOf("?"));
        }

        if ("".equals(filePath) || "/".equals(filePath)) {
            filePath = defaultFile;
        }

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        filePath = filePath.replace("/", File.separator);
        Log.debug("Request received to get file {}", baseDir + File.separator + filePath);
        File in = new File(baseDir + File.separator + filePath);
        if (in.exists() && in.isFile()) {
            try {
                FileInputStream ins = new FileInputStream(in);

                if (HTTPHelper.isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
//                    resp.setHeader("Transfer-Encoding", "chunked");
                    // TODO send chunked
                    writer.write(HTTPHelper.convertStream(ins));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(HTTPHelper.convertStream(ins), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));

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
        filePath = filePath.replace("/", File.separator);
        Log.debug("Request received to get file {}", baseDir + File.separator + filePath);
        File in = new File(baseDir + File.separator + filePath);
        if (in.exists() && in.isFile()) {
            try {
                FileInputStream ins = new FileInputStream(in);

                if (HTTPHelper.isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(HTTPHelper.convertStream(ins));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(HTTPHelper.convertStream(ins), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));

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
        String filePath = req.getPathInfo();
        filePath = origin.applyPatternToRemove(filePath);

        if (filePath.contains("?")) {
            filePath = filePath.substring(0, filePath.indexOf("?"));
        }

        if ("".equals(filePath) || "/".equals(filePath)) {
            filePath = defaultFile;
        }

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        Log.debug("Request received to get file {}", filePath);
        InputStream in = origin.getClass().getClassLoader().getResourceAsStream(filePath);
        if (in != null) {
            try {
                if (HTTPHelper.isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(HTTPHelper.convertStream(in));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(HTTPHelper.convertStream(in), "UTF-8"));
                    writer.flush();
                }
                in.close();
                resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));
                return true;
            } catch (Exception e) {
                Log.error("Unable to load file: {}", e, filePath);
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
                if (HTTPHelper.isRaw(filePath)) {
                    OutputStream writer = resp.getOutputStream();
                    writer.write(HTTPHelper.convertStream(in));
                    writer.flush();
                } else {
                    PrintWriter writer = resp.getWriter();
                    writer.println(new String(HTTPHelper.convertStream(in), "UTF-8"));
                    writer.flush();
                }
                resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));
                return true;
            } catch (Exception e) {
                Log.error("", e);
            }
        } else {
            Log.debug("Resource {} doesn't exist ", filePath);
        }
        return false;
    }
}
