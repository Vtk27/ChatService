package com.chatservice.websocket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatMessageSubscriber implements org.springframework.data.redis.connection.MessageListener {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageSubscriber.class);

    private final SessionRegistry sessionRegistry;

    public ChatMessageSubscriber(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            JSONObject root = new JSONObject(payload);
            String toUserId = root.optString("toUserId", "");
            if (toUserId.isBlank()) {
                return;
            }
            WebSocketSession target = sessionRegistry.getSessionByUserId(toUserId);
            if (target != null && target.isOpen()) {
                target.sendMessage(new TextMessage(root.toString()));
                log.info("Delivered pub/sub message to userId={}", toUserId);
            } else {
                log.warn("No active session for userId={} (pub/sub)", toUserId);
            }
        } catch (Exception e) {
            log.warn("Failed to process pub/sub message", e);
        }
    }
}
