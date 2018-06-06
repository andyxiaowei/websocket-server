package com.codertianwei.websocket.repository;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ExecutionException;

@Component
public class AutoIncrementIdGenerator {
    @Autowired
    protected StringRedisTemplate redisTemplate;

    protected final LoadingCache<Class<?>, RedisAtomicLong> counterCache = CacheBuilder.newBuilder()
            .maximumSize(Long.MAX_VALUE)
            .build(new CacheLoader<Class<?>, RedisAtomicLong>() {
                @Override
                public RedisAtomicLong load(Class<?> clazz) throws Exception {
                    String key = clazz.getCanonicalName() + "_counter";
                    String value = redisTemplate.opsForValue().get(key);
                    Long initialValue = !StringUtils.isEmpty(value) ? Long.valueOf(value) : 10000l;
                    return new RedisAtomicLong(key,
                            redisTemplate.getConnectionFactory(),
                            initialValue);
                }
            });

    public Long getAutoIncrementId(Class<?> clazz) throws ExecutionException {
        return counterCache.get(clazz).incrementAndGet();
    }
}
