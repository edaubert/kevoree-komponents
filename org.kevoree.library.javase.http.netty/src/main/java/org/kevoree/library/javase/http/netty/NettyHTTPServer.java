package org.kevoree.library.javase.http.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;
import org.kevoree.library.javase.http.api.AbstractHTTPServer;
import org.kevoree.library.javase.http.api.HTTPOperationTuple;

import javax.net.ssl.SSLEngine;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/08/13
 * Time: 16:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType(description = "Webbit server to server HTTP request. Thhis implementations is based on servlet API. However webbit doesn't provide a way to do chunked response for binary content. That's why this implementation is not able to stream binary content like media.")
@Requires({
        @RequiredPort(name = "error", type = PortType.MESSAGE, optional = true/*, messageType = HTTPOperationTuple.class.getName()*/)
})
/*@DictionaryType({
        @DictionaryAttribute(name = "ssl", defaultValue = "false", vals = {"true", "false"})
})*/
/*@Provides({
        @ProvidedPort(name = "errorResponse", type = PortType.MESSAGE*//*, messageType = HTTPOperationTuple.class.getName()*//*)
})*/
public class NettyHTTPServer extends AbstractHTTPServer {
    private int port;
    private boolean ssl;
    private ChannelFuture channelFuture;
    private NettyHTTPHandler handler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Override
    public void start() throws Exception {
        port = Integer.parseInt(getDictionary().get("port").toString());
        ssl = getDictionary().get("ssl") != null && "true".equalsIgnoreCase(getDictionary().get("ssl").toString());

        handler = new NettyHTTPHandler();

        try {
            ServerBootstrap b = new ServerBootstrap();
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // Create a default pipeline implementation.
                            ChannelPipeline pipeline = ch.pipeline();

                            // Uncomment the following line if you want HTTPS
                            /*if (ssl) {
                                SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
                                engine.setUseClientMode(false);
                                pipeline.addLast("ssl", new SslHandler(engine));
                            }*/

                            pipeline.addLast("decoder", new HttpRequestDecoder());
                            // Uncomment the following line if you don't want to handle HttpChunks.
                            //p.addLast("aggregator", new HttpObjectAggregator(1048576));
                            pipeline.addLast("encoder", new HttpResponseEncoder());
                            // Remove the following line if you don't want automatic content compression.
                            //p.addLast("deflater", new HttpContentCompressor());
                            pipeline.addLast("handler", handler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            channelFuture = b.bind(port).sync();
        } catch (Exception e) {
            throw e;
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() throws Exception {
        try {
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            throw e;
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void update() throws Exception {
        if (Integer.parseInt(getDictionary().get("port").toString()) != port) {
            stop();
            start();
        }
    }

    @Override
    // TODO replace Object with a specific type and rename the parameter
    public void response(Object param) {
        if (param != null && param instanceof HTTPOperationTuple) {
            handler.response((HTTPOperationTuple) param);
        }
    }

    // TODO replace Object with a specific type and rename the parameter
    void request(/*HTTPOperationTuple*/Object param) {
        if (param != null && param instanceof HTTPOperationTuple && isPortBinded("request")) {
            getPortByName("request", MessagePort.class).process(param);
        }
    }
}
