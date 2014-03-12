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
    protected String templates;

    protected Map<String, String> templatesMap;

    public void setTemplates(String templates) {
        if (templatesMap == null) {
            templatesMap = new HashMap<String, String>();
        }
        this.templates = templates;
        if (templates != null && !"".equals(templates)) {
            String[] templatesDef = templates.split(",");
            for (String templateDef : templatesDef) {
                String[] templateTuple = templateDef.split("=");
                Log.trace("Add template: {}", templateDef);
                if (templateTuple.length == 2) {
                    templatesMap.put(templateTuple[0], templateTuple[1]);
                } else if (templateTuple.length == 1) {
                    // this allow empty value for template
                    templatesMap.put(templateTuple[0], "");
                }
            }
        }
    }

    @Start
    public void start() throws Exception {
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
