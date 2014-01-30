package org.kevoree.library.javase.http.samples.pages;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;
import org.kevoree.annotation.Start;
import org.kevoree.library.javase.http.api.helper.StaticFileHandlerHelper;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/01/14
 * Time: 09:09
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class SimpleTemplatingStaticFileHandler extends StaticFileHandler {
    @Param(optional = true)
    private String templates;

    private Map<String, String> templatesMap;

    @Start
    public void start() throws Exception {
        templatesMap = new HashMap<String, String>();
        if (templates != null && "".equals(templates)) {
            String[] templatesDef = templates.split(",");
            for (String templateDef : templatesDef) {
                String[] templateTuple = templateDef.split("=");
                if (templateTuple.length == 2) {
                    templatesMap.put(templateTuple[0], templateTuple[1]);
                }
            }
        }
        super.start();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.debug("doGet in {} for {}", cmpContext.getInstanceName(), req.getRequestURI());
        if (templates != null && !"".equals(templates)) {
            if (contained) {
                if (!StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp, templatesMap)) {
                    fileNotFound(req, resp);
                }
            } else {
                if (!StaticFileHandlerHelper.checkStaticFileFromDir(defaultFile, path, this, req, resp, templatesMap) && !StaticFileHandlerHelper.checkStaticFile(defaultFile, this, req, resp, templatesMap)) {
                    fileNotFound(req, resp);
                }
            }
        } else {
            super.doGet(req, resp);
        }
    }
}
