package org.kevoree.library.javase.http.samples.pages;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.http.api.page.AbstractParentHTTPHandler;
import org.kevoree.library.javase.http.api.helper.StaticFileHandlerHelper;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    private boolean contained;
    @Param(optional = true, defaultValue = ".")
    private String path;
    @Param(optional = true, defaultValue = "index.html")
    private String defaultFile;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.debug("doGet in {} for {}", cmpContext.getInstanceName(), req.getRequestURI());
        if (contained) {
            if (!StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp)) {
                fileNotFound(req, resp);
            }
        } else {
            if (!StaticFileHandlerHelper.checkStaticFileFromDir(defaultFile, path, this, req, resp) && !StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp)) {
                fileNotFound(req, resp);
            }
        }
    }

    private void fileNotFound(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

}
