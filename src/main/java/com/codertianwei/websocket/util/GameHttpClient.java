package com.codertianwei.websocket.util;

import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Future;

@Component
public class GameHttpClient {
    private static final Logger logger = LogManager.getLogger(GameHttpClient.class);

    private CloseableHttpAsyncClient httpclient;

    public GameHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(2)
                .setSoKeepAlive(true)
                .build();

        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            logger.error("error", e);
        }

        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(100);

        httpclient = HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setThreadFactory(new AffinityThreadFactory("httpclient", AffinityStrategies.SAME_CORE))
                .build();

        httpclient.start();
    }

    @PreDestroy
    public void close() {
        try {
            if (httpclient != null) {
                httpclient.close();
            }
        } catch (IOException e) {
            logger.error("error", e);
        } finally {
            httpclient = null;
        }
    }

    public String doGet(String url) {
        try {
            final HttpGet request = new HttpGet(url);
            Future<HttpResponse> future = httpclient.execute(request, null);
            HttpResponse response = future.get();
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            logger.error("error", e);
            return "";
        }
    }

    private HttpPost getHttpPost(String url,
                                 String body) {
        try {
            logger.info(String.format("request %s %s", url, body));
            final HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(body));
            request.setHeader("Accept", "*/*");
            request.setHeader("Connection", "Keep-Alive");
            request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            return request;
        } catch (Exception e) {
            logger.error("error", e);
            return null;
        }
    }

    public String doPost(String url,
                         String body) {
        try {
            Future<HttpResponse> future = httpclient.execute(getHttpPost(url, body), null);
            HttpResponse response = future.get();
            String result = EntityUtils.toString(response.getEntity());
            logger.info(String.format("response %s", result));
            return result;
        } catch (Exception e) {
            logger.error("error", e);
            return "";
        }
    }

    public void doAsyncPost(String url,
                            String body) {
        httpclient.execute(getHttpPost(url, body), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    String json = EntityUtils.toString(result.getEntity());
                    logger.info(String.format("response %s", json));
                } catch (IOException e) {
                    logger.error("error", e);
                }
            }

            @Override
            public void failed(Exception ex) {
                logger.error("error", ex);
            }

            @Override
            public void cancelled() {

            }
        });
    }
}
