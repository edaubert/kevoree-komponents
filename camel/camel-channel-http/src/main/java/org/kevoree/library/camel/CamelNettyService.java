package org.kevoree.library.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.kevoree.ContainerNode;
import org.kevoree.annotation.ChannelType;
import org.kevoree.api.Port;
import org.kevoree.library.camel.channel.framework.callback.RemoteCamelCallback;
import org.kevoree.log.Log;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 15:25
 */
@ChannelType
public class CamelNettyService extends CamelNettyMessage {

    private Random random = new Random();

    @Override
    protected void buildRoutes(RouteBuilder routeBuilder) {
        routeBuilder.from("kchannel:input")
                    .process(new Processor() {
                        @Override
                        public void process(final Exchange exchange) throws Exception {
                            if (channelContext.getRemotePortPaths().isEmpty() && channelContext.getLocalPorts().isEmpty()) {
                                Log.debug("No consumer, msg lost=" + exchange.getIn().getBody());
                            } else {
                                for (final Port p : channelContext.getLocalPorts()) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                p.call(exchange.getIn().getBody(), callbackFactory.createCamelCallback(new RemoteCamelCallback(exchange)));
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                }
                                for (String remotePortPath : channelContext.getRemotePortPaths()) {
                                    org.kevoree.Port port = modelService.getCurrentModel().getModel().findByPath(remotePortPath, org.kevoree.Port.class);
                                    if (port != null) {
                                        ContainerNode remoteNode = (ContainerNode) port.eContainer().eContainer();
                                        List<String> addresses = getAddresses(remoteNode.getName());
                                        if (addresses.size() > 0) {
                                            for (String address : addresses) {
                                                try {
                                                    Object result = getContext().createProducerTemplate().requestBody("netty:tcp://" + address + ":" + parsePortNumber(remoteNode.getName()), exchange.getIn().getBody());
                                                    // forward the result
                                                    exchange.getOut().setBody(result);
                                                    break;
                                                } catch (Exception e) {
                                                    Log.debug("Unable to send data to components on {} using {} as address", e, remoteNode.getName(), "netty:tcp://" + address + ":" + parsePortNumber(remoteNode.getName()));
                                                }
                                            }
                                        } else {
                                            try {
                                                Object result = getContext().createProducerTemplate().requestBody("netty:tcp://127.0.0.1:" + parsePortNumber(remoteNode.getName()), exchange.getIn().getBody());
                                                // forward the result
                                                exchange.getOut().setBody(result);
                                            } catch (Exception e) {
                                                Log.debug("Unable to send data to components on {} using {} as address", e, remoteNode.getName(), "netty:tcp://127.0.0.1:" + parsePortNumber(remoteNode.getName()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
        List<String> addresses = getAddresses(context.getNodeName());
        if (addresses.size() > 0) {
            for (String address : addresses) {
                try {
                    routeBuilder.from("netty:tcp://" + address + ":" + port + "?sync=true").
                            process(new Processor() {
                                public void process(final Exchange exchange) throws Exception {
                                    for (final Port p : channelContext.getLocalPorts()) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    p.call(exchange.getIn().getBody(), callbackFactory.createCamelCallback(new RemoteCamelCallback(exchange)));
                                                } catch (Exception e) {
                                                }
                                            }
                                        }.start();
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
                            public void process(final Exchange exchange) throws Exception {
                                for (final Port p : channelContext.getLocalPorts()) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                p.call(exchange.getIn().getBody(), callbackFactory.createCamelCallback(new RemoteCamelCallback(exchange)));
                                            } catch (Exception e) {
                                            }
                                        }
                                    }.start();
                                }
                            }
                        });
            } catch (Exception e) {
                Log.debug("Fail to manage route {}", e, "netty:tcp://127.0.0.1:" + port + "?sync=true");
            }
        }
    }
}
