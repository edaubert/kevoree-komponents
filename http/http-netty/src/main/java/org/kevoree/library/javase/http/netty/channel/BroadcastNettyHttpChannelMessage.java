package org.kevoree.library.javase.http.netty.channel;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.annotation.ChannelType;
import org.kevoree.api.Callback;
import org.kevoree.api.Port;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/03/14
 * Time: 18:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ChannelType
public class BroadcastNettyHttpChannelMessage extends AbstractNettyChannel {
    @Override
    public void dispatch(final Object payload, final Callback callback) {
        // we define a specific callback which implements the semantic we want manage (e.g. first result returned is the one submitted to the callback, waiting for all result before returning them together)
//            final Callback firstReturnCallback = callbackFactory.createCamelCallback(callback);
//                Log.trace("Trying to forward message to ChannelCamelComponent.consumerInput");
        if (!channelContext.getRemotePortPaths().isEmpty()) {
            List<String> alreadySentToNodes = new ArrayList<String>();
            for (String remotePortPath : channelContext.getRemotePortPaths()) {
                org.kevoree.Port port = modelService.getCurrentModel().getModel().findByPath(remotePortPath, org.kevoree.Port.class);
                // only send data for provided ports
                if (port != null && ((ComponentInstance) port.eContainer()).getProvided().contains(port)) {
                    ContainerNode remoteNode = (ContainerNode) port.eContainer().eContainer();
                    if (!alreadySentToNodes.contains(remoteNode.path())) {
                        sendData(payload, remoteNode);
                        alreadySentToNodes.add(remoteNode.path());
                    }
                }
            }
        }
        dispatchLocal(payload);
    }

    @Override
    public Object dispatchLocal(Object payload) {
        for (Port p : channelContext.getLocalPorts()) {
            org.kevoree.Port port = modelService.getCurrentModel().getModel().findByPath(p.getPath(), org.kevoree.Port.class);
            if (port != null && ((ComponentInstance) port.eContainer()).getProvided().contains(port)) {
                p.send(payload);
            }
        }
        return null;
    }
}
