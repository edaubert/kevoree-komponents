package org.kevoree.library.javase.http.netty.channel;

import io.netty.handler.codec.serialization.ClassResolver;
import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.ModelService;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/03/14
 * Time: 09:59
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ChannelClassResolver implements ClassResolver {
    private ModelService modelService;
    private BootstrapService bootstrapService;
    private String nodeName;

    private AbstractNettyChannel channel;

    public ChannelClassResolver(ModelService modelService, BootstrapService bootstrapService, String nodeName, AbstractNettyChannel channel) {
        this.modelService = modelService;
        this.bootstrapService = bootstrapService;
        this.nodeName = nodeName;
        this.channel = channel;
    }

    @Override
    public Class<?> resolve(String className) throws ClassNotFoundException {
        ContainerRoot model = modelService.getCurrentModel().getModel();
        ContainerNode node = model.findNodesByID(nodeName);

        Class<?> clazz = null;
        if (node != null) {
            Channel channelInstance = model.findHubsByID(channel.getInstanceName());
            if (channelInstance != null) {
                for (MBinding binding : channelInstance.getBindings()) {
                    if (((ContainerNode) binding.eContainer().eContainer()).getName().equals(node.getName())) {
                        ComponentInstance componentInstance = ((ComponentInstance) binding.eContainer());
                        DeployUnit deployUnit = componentInstance.getTypeDefinition().getDeployUnit();
                        try {
                            clazz = bootstrapService.get(deployUnit).loadClass(className);
                        } catch (ClassNotFoundException ignored) {
                        }
                        if (clazz != null) {
                            break;
                        }
                    }
                }
            } else {
                Log.warn("Unable to find the channel '{}' in current model", channelInstance.getName());
            }
        } else {
            Log.warn("Unable to find the node '{}' in current model", nodeName);
        }
        return clazz;
    }
}
