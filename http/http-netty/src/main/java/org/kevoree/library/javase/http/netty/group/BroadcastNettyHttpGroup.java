package org.kevoree.library.javase.http.netty.group;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.GroupType;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.log.Log;

import java.util.UUID;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/03/14
 * Time: 09:02
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@GroupType
public class BroadcastNettyHttpGroup extends AbstractNettyHttpGroup {

    private boolean isBootstrap = true;

    @Override
    public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot2) {
        return true;
    }

    @Override
    public void modelUpdated() {
        if (isBootstrap) {
            isBootstrap = false;
        } else {
            UUIDModel uuidModel = modelService.getCurrentModel();

            Group modelElement = findModelElement();
            if (modelElement != null) {
                for (ContainerNode subNode : modelElement.getSubNodes()) {
                    if (!sendModelToNode(uuidModel, subNode)) {
                        Log.warn("Unable to send the model to {}", subNode.getName());
                    }
                }
            }
        }
    }

    @Override
    public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot2) {

    }

    @Override
    public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot2) {

    }

    @Override
    boolean updateModel(ContainerRoot model, UUID uuid) {
        Monitor monitor = new Monitor();
        monitor.initialize();

        modelService.unregisterModelListener(this);
        // TODO uuid is not used
        // maybe we need to use the uuid
        // Indeed the uuid is the one on which this new model must be applied.
        // If it is not the current then the update will fail
        modelService.update(model, monitor);
        boolean succeed = monitor.waitFor();

        modelService.registerModelListener(this);
        return succeed;
    }

    private class Monitor implements UpdateCallback {

        private boolean result;
        private boolean done;

        @Override
        public synchronized void run(Boolean aBoolean) {
            result = aBoolean;
            done = true;
        }

        synchronized void initialize() {
            result = false;
            done = false;
        }

        synchronized boolean waitFor() {
            if (!done) {
                try {
                    this.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }
}
