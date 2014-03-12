package org.kevoree.komponents.helpers;

import org.kevoree.api.handler.UpdateCallback;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 10/03/14
 * Time: 17:43
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SynchronizedUpdateCallback implements UpdateCallback {

    private boolean done;
    private boolean result;

    public synchronized void initialize() {
        done = false;
        result = false;
    }

    @Override
    public synchronized void run(Boolean aBoolean) {
        result = aBoolean;
        done = true;
        this.notify();
    }

    public synchronized boolean waitForResult(long timeout) {
        if (!done) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return done && result;
    }
}
