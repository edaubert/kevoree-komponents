package org.kevoree.library.javase.http.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/03/14
 * Time: 12:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyClient {
    private ChannelFuture channelFuture;

    private Bootstrap bootstrap;
    private EventLoopGroup bossGroup;

    private String instanceName;
    private NettyClientHandler handler;

    public void start(final NettyClientHandler handler, final Map<String, ChannelHandler> extraHandlers) {
        // Configure the client.
        bossGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(bossGroup).channel(NioSocketChannel.class)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) throws Exception {
                         // Create a default pipeline implementation.
                         ChannelPipeline pipeline = ch.pipeline();

                         // TODO Uncomment the following line if you want HTTPS
                            /*if (ssl) {
                                SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
                                engine.setUseClientMode(false);
                                pipeline.addLast("ssl", new SslHandler(engine));
                            }*/

                         pipeline.addLast("decoder", new HttpResponseDecoder());
                         pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                         pipeline.addLast("encoder", new HttpRequestEncoder());
                         pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

                         for (String name : extraHandlers.keySet()) {
                             pipeline.addLast(name, extraHandlers.get(name));
                         }

                         pipeline.addLast("handler", handler);
                     }
                 });
        this.handler = handler;
    }

    public void stop() throws InterruptedException {
        try {
            // shut down the server.
            channelFuture.channel().close().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().await();
            }
            channelFuture = null;
        }
    }

    public synchronized NettyClientOutput sendRequest(String host, int port, String uri, InputStream content) {
        try {
            Channel channel = bootstrap.connect(host, port).sync().channel();
            FullHttpRequest request =
                    new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri);
            HttpHeaders headers = request.headers();
            headers.set(HttpHeaders.Names.HOST, host);
            headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
//            headers.set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP.toString() + ','
//                    + HttpHeaders.Values.DEFLATE.toString());

            headers.set(HttpHeaders.Names.ACCEPT_CHARSET, "UTF-8");
//            headers.set(HttpHeaders.Names.REFERER, uri);
            headers.set(HttpHeaders.Names.USER_AGENT, "Kevoree-Komponents HTTP over Netty");

            handler.processRequest(host, port, uri, content, request);
            channel.writeAndFlush(request).sync();

            // Wait for the server to close the connection.
            channel.closeFuture().sync();
            return handler.getOutput();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        /*try {
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

            System.err.println(connection.getResponseCode());

            if (HttpResponseStatus.OK.code() == connection.getResponseCode()) {
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
                System.err.println(new String(byteArrayOutputStream.toByteArray(), "UTF-8"));
                return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;*/
    }
}
