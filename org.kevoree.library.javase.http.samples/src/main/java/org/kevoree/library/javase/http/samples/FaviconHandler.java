package org.kevoree.library.javase.http.samples;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.http.api.AbstractHTTPHandler;
import org.kevoree.library.javase.http.api.StaticFileHandlerHelper;
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
@DictionaryType({
        @DictionaryAttribute(name = "favicon", optional = true, defaultValue = "favicon.ico"),
        @DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/favicon.ico")
})
public class FaviconHandler extends AbstractHTTPHandler {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.debug("doGet in {} for {}", getName(), req.getRequestURI());
        StaticFileHandlerHelper.checkStaticFile(getDictionary().get("favicon").toString(), this, req, resp);
    }
}
