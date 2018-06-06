package com.codertianwei.websocket.service;

import com.alibaba.fastjson.JSONObject;
import com.codertianwei.websocket.event.ServiceEvent;
import com.codertianwei.websocket.util.JSONUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;

@Component
public class ServiceFactory {
    private static final Logger logger = LogManager.getLogger(ServiceFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    protected final LoadingCache<Map.Entry<String, String>, Method> methods = CacheBuilder.newBuilder()
            .maximumSize(Long.MAX_VALUE)
            .build(new CacheLoader<Map.Entry<String, String>, Method>() {
                @Override
                public Method load(Map.Entry<String, String> key) throws Exception {
                    Object service = applicationContext.getBean(key.getKey());
                    Method[] methods = service.getClass().getDeclaredMethods();
                    for (Method method : methods) {
                        WebsocketCommand websocketCommand = method.getDeclaredAnnotation(WebsocketCommand.class);
                        if (websocketCommand != null
                                && key.getValue().equals(key.getValue())) {
                            return method;
                        }
                    }
                    return null;
                }
            });

    protected final LoadingCache<Map.Entry<String, String>, Class<?>> requestBeans = CacheBuilder.newBuilder()
            .maximumSize(Long.MAX_VALUE)
            .build(new CacheLoader<Map.Entry<String, String>, Class<?>>() {
                @Override
                public Class<?> load(Map.Entry<String, String> key) throws Exception {
                    Method method = methods.get(key);
                    RequestBean beanConfig = method.getDeclaredAnnotation(RequestBean.class);
                    String beanName = beanConfig.value();
                    Object bean = applicationContext.getBean(beanName);
                    return bean.getClass();
                }
            });

    public String doWebSocketService(ServiceEvent event) {
        try {
            JSONObject object = JSONUtil.toObject(event.getText());
            if (object == null
                    || object.isEmpty()
                    || !object.containsKey("s")
                    || !object.containsKey("c")
                    || !object.containsKey("p")) {
                return "";
            }
            String serviceName = object.getString("s");
            Object service = applicationContext.getBean(serviceName);
            if (service == null) {
                return "";
            }
            String commandName = object.getString("c");
            SimpleImmutableEntry<String, String> pair = new SimpleImmutableEntry<>(serviceName, commandName);
            Method method = methods.get(pair);
            Object requestBean = object.getObject("p", requestBeans.get(pair));
            Object result = method.invoke(service, requestBean);
            return JSONUtil.toJSON(result);
        } catch (Exception e) {
            logger.error("error", e);
            return "";
        }
    }
}
