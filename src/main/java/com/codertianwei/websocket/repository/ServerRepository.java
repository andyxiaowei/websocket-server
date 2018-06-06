package com.codertianwei.websocket.repository;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface ServerRepository<T> extends Repository<T, String> {
    void delete(T entity);

    Boolean existsById(String primaryKey);

    List<T> findAll();

    Optional<T> findById(String primaryKey) throws ExecutionException;

    void loadAll(Class<T> clazz);

    T save(T entity);

    void saveAll(Map<String, T> entities);

    void saveAll(List<T> entities);
}
