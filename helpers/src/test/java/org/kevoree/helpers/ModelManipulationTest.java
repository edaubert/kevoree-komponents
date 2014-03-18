package org.kevoree.helpers;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Instance;
import org.kevoree.komponents.helpers.ModelManipulation;
import org.kevoree.loader.JSONModelLoader;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/03/14
 * Time: 19:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class ModelManipulationTest {

    @Test
    public void testGetIpsOnlyv4() {

    }

    @Test
    public void testGetIpsAll() {

    }

    @Test
    public void testGetFragmentDictionary() {
        ContainerRoot model = loadModel();
        Instance instance = model.findHubsByID("lbMonitorChannelReceiveSosieInformation");
        Assert.assertNotEquals(instance, null);
        for (ContainerNode node : model.getNodes()) {
            System.out.println(node.getName());
            System.out.println(ModelManipulation.getFragmentDictionaryValue(instance, "port", node.getName()));
        }


    }

    private ContainerRoot loadModel() {
        return (ContainerRoot) new JSONModelLoader().loadModelFromStream(this.getClass().getClassLoader().getResourceAsStream("model1.kev")).get(0);
    }

}
