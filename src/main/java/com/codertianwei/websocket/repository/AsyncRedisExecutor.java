package com.codertianwei.websocket.repository;

import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class AsyncRedisExecutor implements Executor {
    @Value("${redis.async.thread_count}")
    private Integer threadCount;

    private Executor executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(threadCount,
                new AffinityThreadFactory("redis_async", AffinityStrategies.DIFFERENT_CORE));
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executor.execute(command);
    }
}
