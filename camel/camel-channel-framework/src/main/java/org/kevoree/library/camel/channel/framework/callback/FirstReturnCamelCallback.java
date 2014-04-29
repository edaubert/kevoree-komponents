package org.kevoree.library.camel.channel.framework.callback;

import org.kevoree.api.Callback;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/01/14
 * Time: 11:04
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class FirstReturnCamelCallback<T> implements Callback<T> {

    private Callback<T> callback;
    private boolean alreadyDone;

    public FirstReturnCamelCallback(Callback<T> callback) {
        alreadyDone = false;
        this.callback = callback;
    }

    @Override
    public synchronized void onSuccess(T result) {
        if (!alreadyDone) {
            alreadyDone = true;
            if (callback != null) {
                callback.onSuccess(result);
            }
        }
    }

    @Override
    public synchronized void onError(Throwable exception) {
        if (!alreadyDone) {
            alreadyDone = true;
            if (callback != null) {
                callback.onError(exception);
            }
        }
    }
}
