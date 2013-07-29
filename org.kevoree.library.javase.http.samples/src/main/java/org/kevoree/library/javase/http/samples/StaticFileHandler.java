package org.kevoree.library.javase.http.samples;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.http.api.AbstractParentHTTPHandler;
import org.kevoree.library.javase.http.api.StaticFileHandlerHelper;
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
@DictionaryType({
        @DictionaryAttribute(name = "path", optional = true, defaultValue = "."),
        @DictionaryAttribute(name = "contained", defaultValue = "true"),
        @DictionaryAttribute(name = "defaultFile", defaultValue = "index.html"),
})
public class StaticFileHandler extends AbstractParentHTTPHandler {

    private boolean contained;
    private String sourceFolder;
    private String defaultFile;

    @Start
    public void start() {
        contained = getDictionary().get("contained").equals("true");
        sourceFolder = getDictionary().get("path").toString();
        defaultFile = getDictionary().get("defaultFile").toString();
        super.start();
    }

    @Update
    public void update() {
        if (contained != getDictionary().get("contained").equals("true") || !sourceFolder.equals(getDictionary().get("path").toString()) || !defaultFile.equals(getDictionary().get("defaultFile").toString())) {
//            stop();
            start();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.debug("doGet in {} for {}", getName(), req.getRequestURI());
        if (contained) {
            if (!StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp)) {
                fileNotFound(req, resp);
            }
        } else {
            if (!StaticFileHandlerHelper.checkStaticFileFromDir(defaultFile, sourceFolder, this, req, resp) && !StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp)) {
                fileNotFound(req, resp);
            }
        }
    }

    private void fileNotFound(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

}
