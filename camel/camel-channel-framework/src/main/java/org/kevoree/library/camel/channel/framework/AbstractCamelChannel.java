package org.kevoree.library.camel.channel.framework;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.kevoree.annotation.KevoreeInject;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.*;
import org.kevoree.library.camel.channel.framework.callback.CamelCallbackFactory;
import org.kevoree.library.camel.channel.framework.callback.FirstReturnCamelCallback;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/12/13
 * Time: 13:36
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class AbstractCamelChannel implements ChannelDispatch {

    @KevoreeInject
    protected Context context;

    @KevoreeInject
    protected ChannelContext channelContext;

    @KevoreeInject
    protected ModelService modelService;

    protected CamelCallbackFactory callbackFactory;

    private CamelContext camelContext = null;

    public CamelContext getContext() {
        return camelContext;
    }

    public CamelContext buildCamelContext() {
        return new DefaultCamelContext();
    }

    protected abstract void buildRoutes(RouteBuilder rb);

    private ChannelCamelComponent cc = null;

    @Start
    public void start() throws Exception {
        callbackFactory = new CamelCallbackFactory(FirstReturnCamelCallback.class);
        camelContext = buildCamelContext();
        camelContext.setClassResolver(new ClassLoaderClassResolver(this.getClass().getClassLoader()));
        cc = new ChannelCamelComponent(this);
        camelContext.addComponent("kchannel", cc);
        RouteBuilder rb = new RouteBuilder() {
            public void configure() {
                buildRoutes(this);
            }
        };
        camelContext.addRoutes(rb);
        camelContext.start();
    }

    @Stop
    public void stop() throws Exception {
//        isRemotelyConnected = false;
        if (context != null) {
            camelContext.stop();
        }
        cc = null;
        context = null;
    }

    @Override
    public void dispatch(final Object payload, Callback callback) {
        try {
//            Log.info("sending a message: {}", payload);
            // we define a specific callback which implements the semantic we want manage (e.g. first result returned is the one submitted to the callback, waiting for all result before returning them together)
            callback.getClass().getTypeParameters()[0].getClass();
            final Callback firstReturnCallback = callbackFactory.createCamelCallback(callback);
            if (cc.consumerInput != null) {
//                Log.trace("Trying to forward message to ChannelCamelComponent.consumerInput");
                if (!channelContext.getRemotePortPaths().isEmpty()) {
//                    Log.trace("Forwarding message to ChannelCamelComponent.consumerInput because there is remote connection");
                    new Thread() {
                        @Override
                        public void run() {
                            Object result = cc.consumerInput.forwardMessage(payload);
                            firstReturnCallback.onSuccess(result);
                        }
                    }.start();
                }
            } else {
                Log.warn("ChannelCamelComponent does not have a consumerInput so remote connection cannot be used");
            }
            for (Port p : channelContext.getLocalPorts()) {
                p.call(payload, firstReturnCallback);
            }
        } catch (Exception e) {
            Log.error("Unable to dispatch a payload ({}) on {}", e, payload, context.getInstanceName());
        }

    }
}
