package org.kevoree.komponents.helpers;

import org.kevoree.*;
import org.kevoree.log.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/03/14
 * Time: 17:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ModelManipulation {

    public static List<String> getIps(ContainerRoot model, String nodeName, boolean ipv4Only) {
        List<String> addresses = new ArrayList<String>();

        ContainerNode node = model.findNodesByID(nodeName);
        if (node != null) {
            for (NetworkInfo ni : node.getNetworkInformation()) {
                for (NetworkProperty np : ni.getValues()) {
                    if (ni.getName().equalsIgnoreCase("ip") || np.getName().equalsIgnoreCase("ip")) {
                        try {
                            if (InetAddress.getByName(np.getValue()) instanceof Inet4Address) {
                                addresses.add(np.getValue());
                            } else if (!ipv4Only) {
                                String value = np.getValue();
                                if (value.contains("/")) {
                                    value = value.substring(0, value.indexOf("/"));
                                }
                                if (InetAddress.getByName(value) instanceof Inet6Address) {
                                    addresses.add(value);
                                }
                            }
                        } catch (UnknownHostException ignored) {

                        }
                    }
                }
            }
        } else {
            Log.warn("Unable to find the node '{}' in current model", nodeName);
        }
        return addresses;
    }


    public static String getFragmentDictionaryValue(Instance instance, String valueName, String fragmentDictionaryName) {
        if (instance != null) {
            FragmentDictionary fragmentDictionary = instance.findFragmentDictionaryByID(fragmentDictionaryName);
            if (fragmentDictionary != null) {
                DictionaryValue dictionaryValue = fragmentDictionary.findValuesByID(valueName);
                if (dictionaryValue != null) {
                    return dictionaryValue.getValue();
                }
            }
        }
        return null;
    }
}
