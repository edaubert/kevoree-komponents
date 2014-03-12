package org.kevoree.library.javase.http.samples.pages;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Param;
import org.kevoree.library.javase.http.api.helper.StaticFileHandlerHelper;
import org.kevoree.library.javase.http.api.page.AbstractParentHTTPHandler;
import org.kevoree.library.javase.http.samples.pages.helper.CachedKevoreeHttpServletResponse;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/07/13
 * Time: 16:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ComponentType
public class StaticFileHandler extends AbstractParentHTTPHandler {

    @Param(optional = true, defaultValue = "true")
    protected boolean contained;
    @Param(optional = true, defaultValue = ".")
    protected String path;
    @Param(optional = true, defaultValue = "index.html")
    protected String defaultFile;
    @Param(optional = true, defaultValue = "true")
    protected boolean useCache;

    private Map<String, byte[]> cache;
    private CachedKevoreeHttpServletResponse cachedResponse;

    @Override
    public void start() throws Exception {
        super.start();
        cache = Collections.synchronizedMap(new HashMap<String, byte[]>());
        cachedResponse = new CachedKevoreeHttpServletResponse();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO use IF_MODIFIED_SINCE header to know if we need to reload the cache or not
        Log.debug("doGet in {} for {}", cmpContext.getInstanceName(), req.getRequestURI());
        boolean resolved = false;
        if (useCache) {
            byte[] content = cache.get(req.getRequestURI());
            if (content != null && content.length > 0) {
                OutputStream stream = resp.getOutputStream();
                stream.write(content);
                resolved = true;
            }
        }
        if (!resolved) {
            if (contained) {
                if (useCache) {
                    cachedResponse.setResponse(resp);
                    if (!StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, cachedResponse)) {
                        fileNotFound(req, resp);
                    }/* else {
                        byte[] content = cache.get(req.getRequestURI());
                        if (content != null && content.length > 0) {
                            cache.put(req.getRequestURI(), cachedResponse.toByteArray());
                            OutputStream stream = resp.getOutputStream();
                            stream.write(content);
                        } else {
                            // should not be executed
                            fileNotFound(req, resp);
                        }

                    }*/
                    cachedResponse.setResponse(null);
                } else if (!StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp)) {
                    fileNotFound(req, resp);
                }
            } else {
                if (useCache) {
                    cachedResponse.setResponse(resp);
                    if (!StaticFileHandlerHelper.checkStaticFileFromDir(defaultFile, path, this, req, cachedResponse) && !StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, cachedResponse)) {
                        fileNotFound(req, resp);
                    }/* else {
                        byte[] content = cache.get(req.getRequestURI());
                        if (content != null && content.length > 0) {
                            cache.put(req.getRequestURI(), cachedResponse.toByteArray());
                            OutputStream stream = resp.getOutputStream();
                            stream.write(content);
                        } else {
                            // should not be executed
                            fileNotFound(req, resp);
                        }

                    }*/
                    cachedResponse.setResponse(null);
                } else if (!StaticFileHandlerHelper.checkStaticFileFromDir(defaultFile, path, this, req, resp) && !StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp)) {
                    fileNotFound(req, resp);
                }
            }
        }
    }

    protected void fileNotFound(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

}
