package com.codertianwei.websocket.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class LogicEventFactory {
    private static final Logger logger = LogManager.getLogger(ServiceEventFactory.class);

    private final EventBus eventBus;

    private LogicEventFactory() {
        eventBus = new EventBus("logic");
        eventBus.register(new DeadEventLister());
    }

    public void post(Object event) {
        try {
            logger.info(String.format("post %s", event.getClass().getCanonicalName()));
            eventBus.post(event);
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    public void register(Object object) {
        try {
            eventBus.register(object);
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    public void unregister(Object object) {
        try {
            eventBus.unregister(object);
        } catch (Exception e) {
            logger.error("error", e);
        }
    }

    protected class DeadEventLister {
        @Subscribe
        public void subscribe(DeadEvent event) {
            logger.warn("warn: logic dead event detected: source {}, event {}",
                    event.getSource(),
                    event.getEvent());
        }
    }
}
