package org.kevoree.komponents.helpers;

import org.kevoree.api.handler.LockCallBack;

import java.util.UUID;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 10/03/14
 * Time: 17:43
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SynchronizedLockCallback implements LockCallBack {

    private boolean done;
    private UUID uuid;

    public synchronized void initialize() {
        done = false;
        uuid = null;
    }

    public synchronized UUID waitForResult(long timeout) {
        if (!done) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (done) {
            return uuid;
        } else {
            return null;
        }
    }

    @Override
    public synchronized void run(UUID uuid, Boolean aBoolean) {
        if (aBoolean) {
        this.uuid = uuid;
        } else {
            this.uuid = null;
        }
        done = true;
        this.notify();
    }
}
