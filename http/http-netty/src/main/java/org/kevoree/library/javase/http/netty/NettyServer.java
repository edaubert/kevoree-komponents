package org.kevoree.library.javase.http.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.kevoree.log.Log;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/03/14
 * Time: 17:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class NettyServer {
    private ChannelFuture channelFuture;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private RestartListener listener;

    private String instanceName;

    public NettyServer(String instanceName) {
        this.instanceName = instanceName;
    }

    public void start(int port, final NettyHandler handler) throws Exception {

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
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
                         pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                         pipeline.addLast("encoder", new HttpResponseEncoder());
                         pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
                         pipeline.addLast("handler", handler);
                     }
                 });
//                    .option(ChannelOption.SO_BACKLOG, 128)
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        internalStart(port);
    }

    private void internalStart(int port) throws Exception {
        channelFuture = bootstrap.bind(port);
        channelFuture.sync();
        if (!channelFuture.isDone() || !channelFuture.isSuccess() || !channelFuture.channel().isActive()) {
            throw new Exception("Unable to start server " +instanceName + ": Timeout while waiting for connection");
        } else {
            //to be notify when the channel is close and restart it as needed
            if (listener == null) {
                listener = new RestartListener(port);
                channelFuture.channel().closeFuture().addListener(listener);
            }
            Log.debug("Server {} is now started at {}", instanceName, channelFuture.channel().localAddress().toString());
        }
    }

    public void stop() throws InterruptedException {
        try {
            channelFuture.channel().closeFuture().removeListener(listener);
            listener = null;
            // shut down the server.
            channelFuture.channel().close().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().await();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().await();
            }
            channelFuture = null;
        }
    }

    private class RestartListener implements GenericFutureListener<ChannelFuture> {

        int port;

        private RestartListener(int port) {
            this.port = port;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            Log.warn("Server {} has been stopped while Kevoree think is always running. Restarting it", instanceName);
            internalStart(port);
        }
    }
}
