package org.kevoree.library.camel.channel.framework.callback;

import org.kevoree.api.Callback;

import java.lang.reflect.InvocationTargetException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/01/14
 * Time: 11:06
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class CamelCallbackFactory {

    private Class callbackClass;

    public CamelCallbackFactory(Class callbackClass) {
        this.callbackClass = callbackClass;
    }

    public Callback createCamelCallback(Callback callback) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (Callback) callbackClass.getConstructor(Callback.class).newInstance(callback);
    }
}
