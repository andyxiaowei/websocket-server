package com.codertianwei.websocket.websocket;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class WebsocketConfig {
    @Value("${netty.server.backlog}")
    private Integer backlog;

    @Value("${netty.server.port}")
    private Integer port;

    @Value("${netty.server.boss_thread_count}")
    private Integer bossThreadCount;

    @Value("${netty.server.worker_thread_count}")
    private Integer workerThreadCount;

    @Value("${netty.server.ssl}")
    private Boolean ssl;

    @Value("${netty.server.ssl_port}")
    private Integer sslPort;
}
