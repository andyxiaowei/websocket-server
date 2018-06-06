package com.codertianwei.websocket.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.concurrent.ThreadFactory;

@Component
public class WebsocketServer {
    private static final Logger logger = LogManager.getLogger(WebsocketServer.class);

    private ServerBootstrap serverBootstrap = new ServerBootstrap();
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    @Autowired
    private WebsocketConfig websocketConfig;

    @PreDestroy
    public void close() {
        if (boss != null) {
            boss.shutdownGracefully();
        }
        if (worker != null) {
            worker.shutdownGracefully();
        }
    }

    public void start() {
        try {
            // Configure SSL
            final SslContext sslCtx;
            if (websocketConfig.getSsl()) {
                File certificate = new File("server.cer");
                File privateKey = new File("server.keystore");
                sslCtx = SslContextBuilder.forServer(certificate, privateKey)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .clientAuth(ClientAuth.OPTIONAL)
                        .build();
            } else {
                sslCtx = null;
            }

            boss = new NioEventLoopGroup(websocketConfig.getBossThreadCount());
            ThreadFactory threadFactory = new AffinityThreadFactory("netty_worker", AffinityStrategies.DIFFERENT_CORE);
            worker = new NioEventLoopGroup(websocketConfig.getWorkerThreadCount(), threadFactory);
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, websocketConfig.getBacklog())
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer(sslCtx))
                    .bind(websocketConfig.getSsl() ? websocketConfig.getSslPort() : websocketConfig.getPort())
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        } catch (Exception e) {
            logger.error("error", e);
            close();
        }
    }
}
