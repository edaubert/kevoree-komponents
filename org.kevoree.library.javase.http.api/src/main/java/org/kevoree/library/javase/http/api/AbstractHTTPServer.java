package org.kevoree.library.javase.http.api;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/04/13
 * Time: 10:58
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ComponentFragment
@DictionaryType({
        @DictionaryAttribute(name = "port" , defaultValue = "8080"),
        @DictionaryAttribute(name = "timeout" , defaultValue = "5000", optional = true)
})
@Requires({
        @RequiredPort(name = "request", type = PortType.MESSAGE/*, messageType = HTTPOperationTuple.class.getName()*/)
})
@Provides({
        @ProvidedPort(name = "response", type = PortType.MESSAGE/*, messageType = HTTPOperationTuple.class.getName()*/)
})
public abstract class AbstractHTTPServer  extends AbstractComponentType {
    @Start
    abstract public void start() throws Exception;

    @Stop
    abstract public void stop() throws Exception;

    @Update
    abstract public void update() throws Exception;

    @Port(name = "response")
    abstract public void response(/*HTTPOperationTuple*/Object param);
}
