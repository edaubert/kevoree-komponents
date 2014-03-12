package org.kevoree.library.javase.http.netty.group;

import org.kevoree.*;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.*;
import org.kevoree.api.Context;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.library.javase.http.netty.NettyServer;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    private NettyModelHandler handler;
    private NettyServer server;
    private JSONModelSerializer serializer;
    private JSONModelLoader loader;

    abstract boolean updateModel(ContainerRoot model, UUID uuid);

    @Start
    public void start() throws Exception {
        // TODO manage ssl
        handler = new NettyModelHandler(this, modelService, timeout);
        server = new NettyServer(context.getInstanceName());
        server.start(port, handler);
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
            List<String> ips = getIpsForNode(node);
            // get port
            int port = getPortForNode(node);
            boolean received = false;
            InputStream stream = null;
            for (String ip : ips) {
                // TODO uuid is not use
                stream = sendRequest("http://" + ip + ":" + port + "/pull", new ByteArrayInputStream(new byte[0]));
                if (stream != null) {
                    received = true;
                    break;
                }
            }
            if (received && stream != null) {
                return (ContainerRoot) loader.loadModelFromStream(stream).get(0);
            }
        }
        return null;
    }

    public boolean sendModelToNode(UUIDModel uuidModel, ContainerNode node) {
        if (node != null) {
            // get ips
            List<String> ips = getIpsForNode(node);
            // get port
            int port = getPortForNode(node);
            InputStream stream;
            for (String ip : ips) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write("uuid=".getBytes());
                    outputStream.write(uuidModel.getUUID().toString().getBytes());
                    outputStream.write("&".getBytes());
                    outputStream.write("model=".getBytes());
                    serializer.serializeToStream(uuidModel.getModel(), outputStream);
                    outputStream.flush();

                    stream = sendRequest("http://" + ip + ":" + port + "/push", new ByteArrayInputStream(outputStream.toByteArray()));
                    if (stream != null) {
                        String result = readStream(stream);
                        if (result.equalsIgnoreCase("done")) {
                            return true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
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

    private List<String> getIpsForNode(ContainerNode n) {
        List<String> ips = new ArrayList<String>();
        if (n != null) {
            for (NetworkInfo ni : n.getNetworkInformation()) {
                if ("ip".equalsIgnoreCase(ni.getName())) {
                    for (NetworkProperty np : ni.getValues()) {
                        ips.add(np.getValue());
                    }
                } else {
                    for (NetworkProperty np : ni.getValues()) {
                        if ("ip".equalsIgnoreCase(np.getName())) {
                            ips.add(np.getValue());
                        }
                    }
                }
            }
        }
        return ips;
    }

    private int getPortForNode(ContainerNode n) {
        Group modelElement = findModelElement();
        if (modelElement != null) {
            FragmentDictionary fragmentDictionary = modelElement.findFragmentDictionaryByID(n.getName());
            if (fragmentDictionary != null) {
                DictionaryValue portValue = fragmentDictionary.findValuesByID("port");
                if (portValue != null) {
                    try {
                        return Integer.parseInt(portValue.getValue());
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        }
        return 9000;
    }

    private InputStream sendRequest(String urlString, InputStream content) {
        try {
            URL url = new URL(urlString);
            // TODO manage ssl
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[2048];
            int length = content.read(bytes);
            int contentLength = length;
            while (length != -1) {
                byteArrayStream.write(bytes, 0, length);
                length = content.read(bytes);
                contentLength += length;
            }

            connection.setRequestProperty("Content-Length", "" + contentLength);
            OutputStream wr = connection.getOutputStream();
            wr.write(byteArrayStream.toByteArray());
            wr.flush();

            InputStream rd = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            length = content.read(bytes);
            while (length != -1) {
                byteArrayOutputStream.write(bytes, 0, length);
                length = rd.read(bytes);
            }
            wr.close();
            rd.close();
            connection.disconnect();
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
