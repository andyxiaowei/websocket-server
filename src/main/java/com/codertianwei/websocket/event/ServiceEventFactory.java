package com.codertianwei.websocket.event;

import com.codertianwei.websocket.Application;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.ChannelHandlerContext;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

public class ServiceEventFactory {
    private static final Logger logger = LogManager.getLogger(ServiceEventFactory.class);

    private Disruptor<ServiceEvent> serviceDisruptor;

    private ServiceEventFactory() {
        ApplicationContext applicationContext = Application.getInstance().getApplicationContext();

        serviceDisruptor = DisruptorCreator.createDisruptor(ServiceEvent::new,
                applicationContext.getBean(ServiceEventHandler.class));
    }

    public static final class DisruptorCreator<T> {
        public static <T> Disruptor<T> createDisruptor(com.lmax.disruptor.EventFactory eventFactory,
                                                       final EventHandler<? super T>... handlers) {
            Disruptor<T> disruptor = new Disruptor<T>(eventFactory,
                    1024,
                    new AffinityThreadFactory("disruptor", AffinityStrategies.DIFFERENT_CORE),
                    ProducerType.SINGLE,
                    new BusySpinWaitStrategy());
            disruptor.handleEventsWith(handlers);
            disruptor.start();
            return disruptor;
        }
    }

    private static class Lazyholder {
        private static ServiceEventFactory instance = new ServiceEventFactory();
    }

    public static ServiceEventFactory getInstance() {
        return ServiceEventFactory.Lazyholder.instance;
    }

    public void postServiceEvent(ChannelHandlerContext context,
                                 String text) {
        serviceDisruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.setContext(context);
            event.setText(text);
        });
    }
}
