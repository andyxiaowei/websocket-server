package com.codertianwei.websocket.repository;

import com.codertianwei.websocket.util.JSONUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;

public abstract class MemoryRepositoryImpl<T> implements ServerRepository<T> {
    private static final Logger logger = LogManager.getLogger(MemoryRepositoryImpl.class);

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    protected AsyncRedisExecutor asyncRedisExecutor;

    protected Class<T> clazz = null;

    protected Field field = null;

    private final LoadingCache<String, Optional<T>> cache;

    public MemoryRepositoryImpl() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(Long.MAX_VALUE)
                .build(new CacheLoader<String, Optional<T>>() {
                    public Optional<T> load(String key) throws Exception {
                        String json = (String) redisTemplate.opsForHash().get(getRedisKey(clazz), key);
                        if (StringUtils.isEmpty(json)) {
                            return Optional.empty();
                        }
                        T object = JSONUtil.toObject(json, clazz);
                        return Optional.ofNullable(object);
                    }
                });
    }

    @Override
    public void delete(T entity) {
        String pkValue = getPKValue(entity);
        cache.invalidate(pkValue);
        deleteInRedis(pkValue, entity);
    }

    protected void deleteInRedis(String primaryKey,
                                 T entity) {
        asyncRedisExecutor.execute(() -> {
            redisTemplate.opsForHash().delete(getRedisKey(entity), primaryKey);
        });
    }

    @Override
    public Boolean existsById(String primaryKey) {
        return cache.asMap().containsKey(primaryKey);
    }

    @Override
    public List<T> findAll() {
        List<T> list = new ArrayList<>();
        cache.asMap()
                .values()
                .forEach(o -> {
                    o.ifPresent(t -> {
                        list.add(t);
                    });
                });
        return list;
    }

    @Override
    public Optional<T> findById(String primaryKey) throws ExecutionException {
        return cache.get(primaryKey);
    }

    @Override
    public void loadAll(Class<T> clazz) {
        this.clazz = clazz;
        this.field = getPKField(clazz);
        redisTemplate.opsForHash().entries(getRedisKey(clazz)).forEach((k, v) -> {
            cache.put((String) k, Optional.ofNullable(JSONUtil.toObject((String) v, clazz)));
        });
    }

    @Override
    public T save(T entity) {
        String pkValue = getPKValue(entity);
        cache.put(pkValue, Optional.of(entity));
        saveInRedis(pkValue, entity);
        return entity;
    }

    protected void saveInRedis(String primaryKey,
                               T entity) {
        asyncRedisExecutor.execute(() -> {
            redisTemplate.opsForHash().put(getRedisKey(entity), primaryKey, JSONUtil.toJSON(entity));
        });
    }

    @Override
    public void saveAll(Map<String, T> entities) {
        if (CollectionUtils.isEmpty(entities)) return;
        Map<String, Optional<T>> optionalMap = new HashMap<>();
        entities.forEach((k, v) -> optionalMap.put(k, Optional.ofNullable(v)));
        cache.putAll(optionalMap);
        saveAllInRedis(entities);
    }

    @Override
    public void saveAll(List<T> entities) {
        if (CollectionUtils.isEmpty(entities)) return;
        final Map<String, T> map = new HashMap<>();
        entities.forEach(v -> map.put(getPKValue(v), v));
        saveAll(map);
    }

    protected void saveAllInRedis(Map<String, T> entities) {
        if (CollectionUtils.isEmpty(entities)) return;
        final String redisKey = getRedisKey(entities);
        final Map<String, String> map = new HashMap<>();
        entities.forEach((k, v) -> {
            map.put(k, JSONUtil.toJSON(v));
        });
        asyncRedisExecutor.execute(() -> {
            redisTemplate.opsForHash().putAll(redisKey, map);
        });
    }

    @NotNull
    protected Field getPKField(Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            PrimaryKey primaryKey = field.getDeclaredAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    @NotNull
    protected String getPKValue(T entity) {
        try {
            return (String) field.get(entity);
        } catch (Exception e) {
            logger.error("error", e);
        }
        return null;
    }

    @NotNull
    protected String getRedisKey(Class<T> clazz) {
        return clazz.getCanonicalName();
    }

    @NotNull
    protected String getRedisKey(T entity) {
        return entity.getClass().getCanonicalName();
    }

    @NotNull
    protected String getRedisKey(Map<String, T> entities) {
        String redisKey = null;
        for (T entity : entities.values()) {
            redisKey = getRedisKey(entity);
            break;
        }
        return redisKey;
    }
}
