package org.kevoree.komponents.helpers;

import org.kevoree.api.Callback;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 10/03/14
 * Time: 17:43
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SynchronizedChannelCallback<T> implements Callback<T> {

    private boolean done;
    private T result;
    private Throwable throwable;

    public synchronized void initialize() {
        done = false;
        result = null;
    }

    public synchronized T waitForResult(long timeout) throws Throwable {
        if (!done) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (done) {
            if (result == null && throwable != null) {
                throw throwable;
            } else {
                return result;
            }
        } else {
            Log.debug("No result receive before timeout exceeeds...");
            return null;
        }
    }

    @Override
    public synchronized void onSuccess(T t) {
        result = t;
        done = true;
        this.notify();
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        this.throwable = throwable;
        done = true;
        this.notify();
    }
}
