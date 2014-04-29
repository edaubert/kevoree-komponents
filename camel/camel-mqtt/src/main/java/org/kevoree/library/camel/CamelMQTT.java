package org.kevoree.library.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.kevoree.*;
import org.kevoree.annotation.ChannelType;
import org.kevoree.annotation.*;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.ModelListenerAdapter;
import org.kevoree.library.camel.channel.framework.AbstractCamelChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 15:25
 */
@ChannelType
public class CamelMQTT extends AbstractCamelChannel {

    @Param(fragmentDependent = true, optional = true, defaultValue = "false")
    boolean master;
    @Param(optional = true, defaultValue = "1883", fragmentDependent = true)
    protected int port;

    @Param(optional = false)
    protected String topic;
    @Param(defaultValue = "AtLeastOnce", optional = true)
    protected String qos;
    @Param(defaultValue = "false", optional = true)
    protected boolean ssl;

    private RouteBuilder routeBuilder;
    private ModelListener listener;


    /* found a solution to remove */
    @Start
    @Override
    public void start() throws Exception {
        listener = new ModelListenerImpl();
        modelService.registerModelListener(listener);
        super.start();
    }

    @Stop
    @Override
    public void stop() throws Exception {
        super.stop();
        modelService.unregisterModelListener(listener);
    }

    @Override
    protected void buildRoutes(RouteBuilder routeBuilder) {
        this.routeBuilder = routeBuilder;
    }


    private List<String> getServerAddresses(ContainerRoot model) {
        List<String> addresses = new ArrayList<String>();
        Channel modelElement = model.findByPath(context.getPath(), Channel.class);
        for (FragmentDictionary fragmentDictionary : modelElement.getFragmentDictionary()) {
            DictionaryValue value = fragmentDictionary.findValuesByID("master");
            if (value != null && "true".equals(value.getValue())) {

                int port = 1883;
                value = fragmentDictionary.findValuesByID("port");
                if (value != null) {
                    port = Integer.parseInt(value.getValue());
                }

                ContainerNode node = model.findNodesByID(fragmentDictionary.getName());
                if (node != null) {
                    for (NetworkInfo ni : node.getNetworkInformation()) {
                        for (NetworkProperty np : ni.getValues()) {
                            if (np.getName() != null && ("ip".equalsIgnoreCase(np.getName()) || np.getName().toLowerCase().startsWith("ip") || np.getName().toLowerCase().endsWith("ip"))) {
                                addresses.add(np.getValue() + ":" + port);
                            }
                        }
                    }
                }
                if (addresses.size() == 0) {
                    addresses.add("127.0.0.1" + ":" + port);
                }
                // we allow multiple master
//                    break;
            }
        }
        return addresses;
    }

    private class ModelListenerImpl extends ModelListenerAdapter {

        private boolean starting = true;

        @Override
        public synchronized void modelUpdated() {
            for (RouteDefinition routeDefinition : routeBuilder.getRouteCollection().getRoutes()) {
                try {
                    getContext().stopRoute(routeDefinition.getShortName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ContainerRoot model;
            if (starting) {
                starting = false;
                model = modelService.getPendingModel();
            } else {
                model = modelService.getCurrentModel().getModel();
            }

            List<String> addresses = getServerAddresses(model);
            String prefixAddress = "tcp://";
            if (ssl) {
                prefixAddress = "ssl://";
            }
            for (String address : addresses) {
                routeBuilder.from("mqtt://" + context.getInstanceName() + "?host=" + prefixAddress + address + "&QoS=" + qos + "&subscribeTopicName=" + topic + "&byDefaultRetain=false");
                routeBuilder.from("kchannel:input").transform(routeBuilder.body().convertToString()).to("mqtt://" + context.getInstanceName() + "?host=" + prefixAddress + address + "&QoS=" + qos + "&publishTopicName=" + topic + "&byDefaultRetain=false");
            }
        }
    }

}
