package org.opengeo.console.monitor.transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.geoserver.monitor.RequestData;

/**
 * Queue messages as they come in, and then send them all at fixed delays
 *
 */
public class QueuedMessageTransport implements MessageTransport, Runnable {

    private final MessageTransport transporter;

    private final ConcurrentLinkedQueue<RequestData> messageQueue;

    private final ScheduledExecutorService executorService;

    public QueuedMessageTransport(MessageTransport transporter, int secondsToPoll) {
        this.transporter = transporter;
        messageQueue = new ConcurrentLinkedQueue<RequestData>();
        executorService = Executors.newScheduledThreadPool(1);
        executorService
                .scheduleWithFixedDelay(this, secondsToPoll, secondsToPoll, TimeUnit.SECONDS);
    }

    @Override
    public void transport(Collection<RequestData> data) {
        messageQueue.addAll(data);
    }

    @Override
    public void run() {
        if (!messageQueue.isEmpty()) {
            RequestData data = null;
            Collection<RequestData> dataToSend = new ArrayList<RequestData>(messageQueue.size());
            while ((data = messageQueue.poll()) != null) {
                dataToSend.add(data);
            }
            transporter.transport(dataToSend);
        }
    }

    @Override
    public void destroy() {
        executorService.shutdown();
        transporter.destroy();
    }
}
