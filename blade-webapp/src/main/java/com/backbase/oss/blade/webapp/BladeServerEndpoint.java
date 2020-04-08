package com.backbase.oss.blade.webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.LinkedHashSet;
import java.util.Set;

@ServerEndpoint(value = "/blade_ws")
public class BladeServerEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BladeServerEndpoint.class.getName());
    private static final Set<Session> sessions = new LinkedHashSet<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final BladeRegistry bladeRegistry = BladeRegistry.getInstance();

    public BladeServerEndpoint() {
        super();
        bladeRegistry.addPropertyChangeListener(evt -> sendText(evt.getNewValue()));
    }

    private static void sendText(Object message) {
        String text = null;
        try {
            text = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.info("Cannot map object: {} to json message", message);
        }
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.getAsyncRemote().sendText(text);
                    }
                } catch (IllegalStateException e) {
                    sessions.remove(session);
                    logger.info("Cannot send message: {} to client: {}", text, session.getId());
                }
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("Received message from client: {} ", message);
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        BladeRegistry.getInstance().refresh();
        sendText(BladeRegistry.getInstance().getBlades().values());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }
}
