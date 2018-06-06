package com.codertianwei.websocket.event;

import com.codertianwei.websocket.service.ServiceFactory;
import com.lmax.disruptor.EventHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceEventHandler implements EventHandler<ServiceEvent> {
    private static final Logger logger = LogManager.getLogger(ServiceEventHandler.class);

    @Autowired
    private ServiceFactory serviceFactory;

    @Override
    public void onEvent(ServiceEvent event, long sequence, boolean endOfBatch) throws Exception {
        String request = event.getText();
        logger.info(String.format("websocket request: %s", request));
        try {
            String response = serviceFactory.doWebSocketService(event);
            event.getContext().channel().writeAndFlush(new TextWebSocketFrame(response));
            logger.info(String.format("websocket response: %s", response));
        } catch (Exception e) {
            logger.error("error", e);
            event.getContext().channel().writeAndFlush(new TextWebSocketFrame(request));
        }
    }
}
