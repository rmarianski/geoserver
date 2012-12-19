package org.geoserver.monitor;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geoserver.platform.ExtensionPriority;
import org.opengeo.console.monitor.transport.MessageTransport;

public class MessageTransportPostProcessor implements RequestPostProcessor, ExtensionPriority {

    private final MessageTransport transporter;

    public MessageTransportPostProcessor(MessageTransport transporter) {
        this.transporter = transporter;
    }

    @Override
    public void run(RequestData data, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        transporter.transport(Collections.singletonList(data));
    }

    @Override
    public int getPriority() {
        // we want this extension to run last
        // this allows all others to run first, and then the transport happens last
        return ExtensionPriority.HIGHEST;
    }

}
