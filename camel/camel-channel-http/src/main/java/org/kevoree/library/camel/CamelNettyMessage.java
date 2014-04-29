package org.kevoree.library.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.kevoree.*;
import org.kevoree.annotation.ChannelType;
import org.kevoree.annotation.Param;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.Port;
import org.kevoree.library.camel.channel.framework.AbstractCamelChannel;
import org.kevoree.log.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 15:25
 */
@ChannelType
public class CamelNettyMessage extends AbstractCamelChannel {

    @Param(optional = true, defaultValue = "10000", fragmentDependent = true)
    protected int port;

    /* found a solution to remove */
    @Start
    @Override
    public void start() throws Exception {
        super.start();
    }

    @Stop
    @Override
    public void stop() throws Exception {
        super.stop();
    }

    @Override
    protected void buildRoutes(RouteBuilder routeBuilder) {
        routeBuilder.from("kchannel:input")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Log.info("sending a message: {}", exchange.getIn().getBody());
                            if (channelContext.getRemotePortPaths().isEmpty() && channelContext.getLocalPorts().isEmpty()) {
                                Log.debug("No consumer, msg lost=" + exchange.getIn().getBody());
                            } else {
                                for (Port p : channelContext.getLocalPorts()) {
                                    p.send(exchange.getIn().getBody());
                                }
                                for (String remotePortPath : channelContext.getRemotePortPaths()) {
                                    org.kevoree.Port port = modelService.getCurrentModel().getModel().findByPath(remotePortPath, org.kevoree.Port.class);
                                    if (port != null) {
                                        // FIXME potential duplicate remoteNode
                                        ContainerNode remoteNode = (ContainerNode) port.eContainer().eContainer();
                                        List<String> addresses = getAddresses(remoteNode.getName());
                                        if (addresses.size() > 0) {
                                            for (String address : addresses) {
                                                try {
                                                    getContext().createProducerTemplate().sendBody("netty:tcp://" + address + ":" + parsePortNumber(remoteNode.getName()), exchange.getIn().getBody());
                                                    break;
                                                } catch (Exception e) {
                                                    Log.debug("Unable to send data to components on {} using {} as address", e, remoteNode.getName(), "netty:tcp://" + address + ":" + parsePortNumber(remoteNode.getName()));
                                                }
                                            }
                                        } else {
                                            try {
                                                getContext().createProducerTemplate().sendBody("netty:tcp://127.0.0.1:" + parsePortNumber(remoteNode.getName()), exchange.getIn().getBody());
                                            } catch (Exception e) {
                                                Log.debug("Unable to send data to components on {} using {} as address", e, remoteNode.getName(), "netty:tcp://127.0.0.1:" + parsePortNumber(remoteNode.getName()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    );
        List<String> addresses = getAddresses(context.getNodeName());
        /*if (addresses.size() > 0) {
            for (String address : addresses) {
                try {
                    routeBuilder.from("netty:tcp://" + address + ":" + port + "?sync=true").
                            process(new Processor() {
                                public void process(Exchange exchange) throws Exception {
                                    exchange.getOut().setBody("result Async TODO");
                                    for (Port p : channelContext.getLocalPorts()) {
                                        p.send(exchange.getIn().getBody());
                                    }
                                }
                            });
                } catch (Exception e) {
                    Log.debug("Fail to manage route {}", e, "netty:tcp://" + address + ":" + port + "?sync=true");
                }
            }
        } else {
            try {
                routeBuilder.from("netty:tcp://127.0.0.1:" + port + "?sync=true").
                        process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                exchange.getOut().setBody("result Async TODO");
                                for (Port p : channelContext.getLocalPorts()) {
                                    p.send(exchange.getIn().getBody());
                                }
                            }
                        });
            } catch (Exception e) {
                Log.debug("Fail to manage route {}", e, "netty:tcp://127.0.0.1:" + port + "?sync=true");
            }
        }*/
        routeBuilder.from("netty:tcp://0.0.0.0:" + port + "?sync=true").
                process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setBody("result Async TODO");
                        for (Port p : channelContext.getLocalPorts()) {
                            p.send(exchange.getIn().getBody());
                        }
                    }
                });
    }


    public List<String> getAddresses(String nodeName) {
        List<String> addresses = new ArrayList<String>();

        ContainerNode node = modelService.getCurrentModel().getModel().findNodesByID(nodeName);
        if (node == null) {
            node = modelService.getPendingModel().findNodesByID(nodeName);
        }
        if (node != null) {
            for (NetworkInfo ni : node.getNetworkInformation()) {
                for (NetworkProperty np : ni.getValues()) {
                    if (ni.getName().equalsIgnoreCase("ip") || np.getName().equalsIgnoreCase("ip")) {
                        try {
                            if (InetAddress.getByName(np.getValue()) instanceof Inet4Address) {
                                addresses.add(np.getValue());
                            } else if (np.getValue().contains("/")) {
                                if (InetAddress.getByName(np.getValue().substring(0, np.getValue().indexOf("/"))) instanceof Inet6Address) {
                                    addresses.add(np.getValue());
                                }
                            }
                        } catch (UnknownHostException ignored) {

                        }
                    }
                }
            }
        }
        return addresses;
    }

    public int parsePortNumber(String nodeName) {
        int port = 10000;
        FragmentDictionary fragmentDictionary = modelService.getCurrentModel().getModel().findHubsByID(context.getInstanceName()).findFragmentDictionaryByID(nodeName);
        if (fragmentDictionary == null) {
            fragmentDictionary = modelService.getPendingModel().findHubsByID(context.getInstanceName()).findFragmentDictionaryByID(nodeName);
        }
        if (fragmentDictionary != null) {

            DictionaryValue dValue = fragmentDictionary.findValuesByID("port");
            if (dValue != null && dValue.getValue() != null) {
                try {
                    port = Integer.parseInt(dValue.getValue());
                } catch (NumberFormatException e) {
                    Log.warn("Attribute \"port\" of {} is not an Integer, Default value ({}) is returned", context.getInstanceName(), port + "");
                }
            } else {
                Log.info("Attribute \"port\" of {} is not set for {}, Default value ({}) is used", context.getInstanceName(), nodeName, port + "");
            }
        } else {
            Log.info("Unable to find Fragment Dictionary in {} for {}, Default value ({}) is used", context.getInstanceName(), nodeName, port + "");
        }
        return port;
    }


}
