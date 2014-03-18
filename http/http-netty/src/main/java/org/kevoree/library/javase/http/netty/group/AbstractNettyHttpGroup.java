package org.kevoree.library.javase.http.netty.group;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.api.Context;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.komponents.helpers.ModelManipulation;
import org.kevoree.library.javase.http.netty.NettyClient;
import org.kevoree.library.javase.http.netty.NettyClientHandler;
import org.kevoree.library.javase.http.netty.NettyClientOutput;
import org.kevoree.library.javase.http.netty.NettyServer;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/03/14
 * Time: 17:06
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@GroupType
public abstract class AbstractNettyHttpGroup implements ModelListener {
    @Param(optional = true, defaultValue = "9000", fragmentDependent = true)
    protected int port;
    @Param(optional = true, defaultValue = "5000")
    protected int timeout;
    @KevoreeInject
    protected ModelService modelService;
    @KevoreeInject
    protected Context context;

    //    private boolean ssl;
    private NettyModelServerHandler serverHandler;
    private NettyServer server;
    private NettyClientHandler clientHandler;
    private NettyClient client;

    private JSONModelSerializer serializer;
    private JSONModelLoader loader;

    abstract boolean updateModel(ContainerRoot model, UUID uuid);

    @Start
    public void start() throws Exception {
        // TODO manage ssl
        serverHandler = new NettyModelServerHandler(this, modelService, timeout);
        server = new NettyServer(context.getInstanceName());
        server.start(port, serverHandler, new HashMap<String, ChannelHandler>());

        clientHandler = new NettyModelClientHandler();
        client = new NettyClient();
        client.start(clientHandler, new HashMap<String, ChannelHandler>());

        serializer = new JSONModelSerializer();
        loader = new JSONModelLoader();
        modelService.registerModelListener(this);
    }

    @Stop
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
        modelService.unregisterModelListener(this);
    }

    public ContainerRoot getModelFromNode(ContainerNode node) {
        if (node != null) {
            // get ips
            List<String> ips = ModelManipulation.getIps((ContainerRoot) node.eContainer(), node.getName(), false);
            // get port
            int port = 9000;
            try {
                port = Integer.parseInt(ModelManipulation.getFragmentDictionaryValue(((ContainerRoot) node.eContainer()).findGroupsByID(context.getInstanceName()), "port", node.getName()));
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
            NettyClientOutput output = null;
            for (String ip : ips) {
                // TODO uuid is not use
                output = client.sendRequest(ip, port, "/pull", new ByteArrayInputStream(new byte[0]));
                if (output != null) {
                    break;
                }
            }
            if (output != null) {
                return (ContainerRoot) loader.loadModelFromStream(output.getContent()).get(0);
            }
        }
        return null;
    }

    public boolean sendModelToNode(UUIDModel uuidModel, ContainerNode node) {
        if (node != null) {
            // get ips
            List<String> ips = ModelManipulation.getIps(uuidModel.getModel(), node.getName(), false);
            // get port
            int port = 9000;
            try {
                port = Integer.parseInt(ModelManipulation.getFragmentDictionaryValue(uuidModel.getModel().findGroupsByID(context.getInstanceName()), "port", node.getName()));
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
            Log.trace("Port for sending model to {} is {}", node.getName(), port);
            NettyClientOutput output;
            for (String ip : ips) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write("uuid=".getBytes());
                    outputStream.write(uuidModel.getUUID().toString().getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write("model=".getBytes());
                    serializer.serializeToStream(uuidModel.getModel(), outputStream);
                    outputStream.flush();

                    output = client.sendRequest(ip, port, "/push", new ByteArrayInputStream(outputStream.toByteArray()));
                    if (output != null) {
                        if (output.getResponseCode() == HttpResponseStatus.OK.code()) {
                            return true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.trace("After trying to send the model on node {}, none of the tries succeed", node.getName());
            return false;
        }
        Log.debug("Unable to send a model to a NULL node");
        return false;
    }

    public boolean setTracesToNode(ContainerNode node) {
        // TODO implement
        return false;
    }

    protected Group findModelElement() {
        Group modelElement = modelService.getCurrentModel().getModel().findGroupsByID(context.getInstanceName());
        if (modelElement == null) {
            Log.error("Unable to find the model element corresponding to {} while this element is running !", context.getInstanceName());
        }
        return modelElement;
    }

    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[2048];
        int length = inputStream.read(bytes);
        while (length != -1) {
            outputStream.write(bytes, 0, length);
            length = inputStream.read(bytes);
        }
        return new String(outputStream.toByteArray(), "UTF-8");
    }



    /*private def sendModel (password: String, sshKey: String, address: String, model: ContainerRoot): Boolean = {
        val bodyBuilder = new StringBuilder
    *//*
        bodyBuilder append "login="
        bodyBuilder append URLEncoder.encode(login, "UTF-8")*//*
        //    bodyBuilder append "&password="
        bodyBuilder append "password="
        bodyBuilder append URLEncoder.encode(password, "UTF-8")
        bodyBuilder append "&ssh_key="
        bodyBuilder append URLEncoder.encode(sshKey, "UTF-8")
        bodyBuilder append "&model="
        bodyBuilder append URLEncoder.encode(KevoreeXmiHelper.saveToString(model, false), "UTF-8")

        logger.debug("url=>" + address)
        try {
            val url = new URL(address)
            val connection = url.openConnection().asInstanceOf[HttpURLConnection]
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Content-Length", "" + Integer.toString(bodyBuilder.length))
            connection.setConnectTimeout(3000)
            connection.setDoOutput(true)
            val wr: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
            wr.write(bodyBuilder.toString())
            wr.flush()
            val rd = connection.getInputStream
            val bytes = new Array[Byte](2048)
            var length = 0
            val byteArrayOutputStream = new ByteArrayOutputStream()
            length = rd.read(bytes)
            while (length != -1) {
                byteArrayOutputStream.write(bytes, 0, length)
                length = rd.read(bytes)
            }
            wr.close()
            rd.close()

            var response = new String(byteArrayOutputStream.toByteArray, "UTF-8")

            var nbTry = 20;
            // look the answer to know if the model has been correctly sent
            while (response.startsWith("<wait") && nbTry > 0) {
                logger.debug(response)
                nbTry = nbTry - 1
                Thread.sleep(3000)
                try {
                    //          val url = new URL(address + "/" + login)
                    val connection = url.openConnection()
                    val rd = connection.getInputStream
                    val bytes = new Array[Byte](2048)
                    var length = 0
                    val byteArrayOutputStream = new ByteArrayOutputStream()
                    length = rd.read(bytes)
                    while (length != -1) {
                        byteArrayOutputStream.write(bytes, 0, length)
                        length = rd.read(bytes)
                    }
                    wr.close()
                    rd.close()
                    response = new String(byteArrayOutputStream.toByteArray, "UTF-8")
                } catch {
                    case _@e =>
                }
            }
            if (response.startsWith("<wait")) {
                logger.debug("Timeout, unable to get your configuration model on the Kloud")
                false
            } else if (response.startsWith("<nack")) {
                val errorMessage = URLDecoder.decode(response, "UTF-8").split("error=\"")(1)
                logger.debug("Unable to submit or sink your model on the Kloud: {}", errorMessage.substring(0, errorMessage.indexOf("\"")))
                false
            } else {
                val lcommand = new LoadModelCommand()
                editor.getPanel.getKernel.getModelHandler.merge(KevoreeXmiHelper.loadString(response))
                PositionedEMFHelper.updateModelUIMetaData(editor.getPanel.getKernel)
                lcommand.setKernel(editor.getPanel.getKernel)
                lcommand.execute(editor.getPanel.getKernel.getModelHandler.getActualModel)
                true
            }
        } catch {
            case _@e => logger.error("Unable to deploy on Kloud", e)
                false
        }
    }*/
/*    class sslpost {
        public void main(String[] args) {
            String cuki = new String();
            try {
                System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
                java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                URL url = new URL("https://www.sunpage.com.sg/sso/login.asp");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestMethod("POST");
                connection.setFollowRedirects(true);


                String query = "UserID=" + URLEncoder.encode("williamalex@hotmail.com");
                query += "&";
                query += "password=" + URLEncoder.encode("password");
                query += "&";
                query += "UserChk=" + URLEncoder.encode("Bidder");
// This particular website I was working with, required that the referrel URL should be from this URL
// as specified the previousURL. If you do not have such requirement you may omit it.
                query += "&";
                query += "PreviousURL=" + URLEncoder.encode("https://www.sunpage.com.sg/sso/login.asp");


//connection.setRequestProperty("Accept-Language","it");
//connection.setRequestProperty("Accept", "application/cfm, image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, image/png, ///");
//connection.setRequestProperty("Accept-Encoding","gzip");


                connection.setRequestProperty("Content-length", String.valueOf(query.length()));
                connection.setRequestProperty("Content-Type", "application/x-www- form-urlencoded");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98; DigExt)");


// open up the output stream of the connection
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());


// write out the data
                int queryLength = query.length();
                output.writeBytes(query);
//output.close();


                System.out.println("Resp Code:" + connection.getResponseCode());
                System.out.println("Resp Message:" + connection.getResponseMessage());


// get ready to read the response from the cgi script
                DataInputStream input = new DataInputStream(connection.getInputStream());


// read in each character until end-of-stream is detected
                for (int c = input.read(); c != -1; c = input.read())
                    System.out.print((char) c);
                input.close();
            } catch (Exception e) {
                System.out.println("Something bad just happened.");
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }*/
}
