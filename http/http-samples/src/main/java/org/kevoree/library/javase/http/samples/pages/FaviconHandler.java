package org.kevoree.library.javase.http.samples.pages;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Param;
import org.kevoree.library.javase.http.api.page.AbstractHTTPHandler;
import org.kevoree.library.javase.http.api.helper.StaticFileHandlerHelper;
import org.kevoree.log.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/07/13
 * Time: 09:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class FaviconHandler extends AbstractHTTPHandler {

    @Param(optional = true, defaultValue = "favicon.png")
    private String favicon;
    @Param(optional = true, defaultValue = "/favicon.ico")
    private String urlPattern;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.debug("doGet in {} for {}", cmpContext.getInstanceName(), req.getRequestURI());
        StaticFileHandlerHelper.checkStaticFile(favicon, this, resp);
    }
}
