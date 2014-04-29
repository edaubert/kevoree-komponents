package org.kevoree.library.camel.channel.framework.callback;

import org.apache.camel.Exchange;
import org.kevoree.api.Callback;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/01/14
 * Time: 18:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class RemoteCamelCallback<T> implements Callback<T> {
    private Exchange exchange;

    public RemoteCamelCallback(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void onSuccess(T result) {
        exchange.getOut().setBody(result);
    }

    @Override
    public void onError(Throwable exception) {

    }
}
