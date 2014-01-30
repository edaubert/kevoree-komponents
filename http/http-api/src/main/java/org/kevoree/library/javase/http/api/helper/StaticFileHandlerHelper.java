package org.kevoree.library.javase.http.api.helper;

import org.kevoree.library.javase.http.api.page.AbstractHTTPHandler;
import org.kevoree.log.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/07/13
 * Time: 16:41
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class StaticFileHandlerHelper {

    private static String buildFilePath(String defaultFile, AbstractHTTPHandler origin, HttpServletRequest req) {
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
        return filePath;
    }

    private static void readFileFromDir(String filePath, String baseDir, OutputStream stream) throws Exception {
        filePath = filePath.replace("/", File.separator);
        Log.debug("Request received to get file {}", baseDir + File.separator + filePath);
        File in = new File(baseDir + File.separator + filePath);
        if (in.exists() && in.isFile()) {
            FileInputStream ins = new FileInputStream(in);
            stream.write(HTTPHelper.convertStream(ins));
            stream.flush();

        } else {
            throw new Exception("Unable to get File: " + in.getAbsolutePath());
        }
    }

    private static void readFile(String filePath, AbstractHTTPHandler origin, OutputStream stream) throws Exception {
        Log.debug("Request received to get file {}", filePath);
        InputStream in = origin.getClass().getClassLoader().getResourceAsStream(filePath);
        stream.write(HTTPHelper.convertStream(in));
        stream.flush();
    }

    public static boolean checkStaticFileFromDir(String defaultFile, String baseDir, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp) {
        return checkStaticFileFromDir(buildFilePath(defaultFile, origin, req), baseDir, resp);
    }

    public static boolean checkStaticFileFromDir(String filePath, String baseDir, HttpServletResponse resp) {
        try {
            OutputStream outputStream = resp.getOutputStream();
            readFileFromDir(filePath, baseDir, outputStream);
            outputStream.flush();
            resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));
            return true;
        } catch (Exception e) {
            Log.error("Unable to read the file: {}", e, baseDir + File.separator + filePath);
            return false;
        }
    }


    public static boolean checkStaticFile(String defaultFile, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp) {
        return checkStaticFile(buildFilePath(defaultFile, origin, req), origin, resp);
    }

    public static boolean checkStaticFile(String filePath, AbstractHTTPHandler origin, HttpServletResponse resp) {
        try {
            OutputStream outputStream = resp.getOutputStream();
            readFile(filePath, origin, outputStream);
            outputStream.flush();
            resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));
            return true;
        } catch (Exception e) {
            Log.error("Unable to read the file: {}", e, filePath);
            return false;
        }
    }


    private static void manageTemplates(StringBuilder contentBuilder, Map<String, String> templates) {
        int index;
        for (String key : templates.keySet()) {
            String template = '{' + key + '}';
            System.err.println(templates.get(key));
            index = 0;
            index = contentBuilder.indexOf(template, index);
            while (index != -1) {
                contentBuilder.replace(index, index + template.length(), templates.get(key));
                index = contentBuilder.indexOf(template, index);
            }
        }
    }

    public static boolean checkStaticFileFromDir(String defaultFile, String baseDir, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp, Map<String, String> templates) {
        return checkStaticFileFromDir(buildFilePath(defaultFile, origin, req), baseDir, resp, templates);
    }

    public static boolean checkStaticFileFromDir(String filePath, String baseDir, HttpServletResponse resp, Map<String, String> templates) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            readFileFromDir(filePath, baseDir, outputStream);
            outputStream.flush();
            if (!HTTPHelper.isRaw(filePath)) {
                StringBuilder contentBuilder = new StringBuilder(outputStream.toString("UTF-8"));
                manageTemplates(contentBuilder, templates);

                System.out.println(contentBuilder.toString());
                outputStream.close();
                outputStream = new ByteArrayOutputStream();
                outputStream.write(contentBuilder.toString().getBytes());
                outputStream.flush();
            }
            OutputStream stream = resp.getOutputStream();
            stream.write(outputStream.toByteArray());
            stream.flush();
            resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));
            return true;
        } catch (Exception e) {
            Log.error("Unable to read the file: {}", e, baseDir + File.separator + filePath);
            return false;
        }
    }


    public static boolean checkStaticFile(String defaultFile, AbstractHTTPHandler origin, HttpServletRequest req, HttpServletResponse resp, Map<String, String> templates) {
        return checkStaticFile(buildFilePath(defaultFile, origin, req), origin, resp, templates);
    }

    public static boolean checkStaticFile(String filePath, AbstractHTTPHandler origin, HttpServletResponse resp, Map<String, String> templates) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            readFile(filePath, origin, outputStream);
            outputStream.flush();
            if (!HTTPHelper.isRaw(filePath)) {
                StringBuilder contentBuilder = new StringBuilder(outputStream.toString("UTF-8"));
                manageTemplates(contentBuilder, templates);

                System.out.println(contentBuilder.toString());
                outputStream.close();
                outputStream = new ByteArrayOutputStream();
                outputStream.write(contentBuilder.toString().getBytes());
                outputStream.flush();
            }
            OutputStream stream = resp.getOutputStream();
            stream.write(outputStream.toByteArray());
            stream.flush();
            resp.addHeader("Content-Type", (HTTPHelper.getHttpHeaderFromURL(filePath)));
            return true;
        } catch (Exception e) {
            Log.error("Unable to read the file: {}", e, filePath);
            return false;
        }
    }

}
