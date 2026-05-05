package com.chatservice.websocket;

import com.chatservice.dao.MessageDao;
import com.github.f4b6a3.uuid.UuidCreator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleWebSocketHandler.class);
    private static final String CHAT_USERS_KEY_PREFIX = "chat:";
    private static final String CHAT_USERS_KEY_SUFFIX = ":users";

    private final StringRedisTemplate stringRedisTemplate;
    private final SessionRegistry sessionRegistry;
    private final MessageDao messageDao;

    public SimpleWebSocketHandler(StringRedisTemplate stringRedisTemplate,
                                  SessionRegistry sessionRegistry,
                                  MessageDao messageDao) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.sessionRegistry = sessionRegistry;
        this.messageDao = messageDao;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Connection established; keep session open for now.
        String userId = extractUserId(session.getUri());
        String chatId = extractChatId(session.getUri());
        sessionRegistry.addSession(session, userId);
        log.info("Session added to in-memory map: sessionId={}", session.getId());
        if (StringUtils.hasText(userId)) {
            log.info("WebSocket user connected: userId={}, sessionId={}", userId, session.getId());
        }
        log.info("WebSocket session established: id={}, uri={}", session.getId(), session.getUri());

        if (StringUtils.hasText(chatId) && StringUtils.hasText(userId)) {
            log.info("userJoined check: chatId={}, userId={}", chatId, userId);
            notifyUserJoined(chatId, userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = sessionRegistry.getUserIdBySessionId(session.getId());
        sessionRegistry.removeSession(session);
        log.info("Session removed from in-memory map: sessionId={}", session.getId());
        if (StringUtils.hasText(userId)) {
            log.info("User session removed from in-memory map: userId={}, sessionId={}", userId, session.getId());
            removeRedisSession(userId);
        }
        log.info("WebSocket session closed: id={}, status={}", session.getId(), status);
    }

    private String extractUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                if ("userId".equalsIgnoreCase(key)) {
                    return pair.substring(idx + 1);
                }
            }
        }
        return null;
    }

    private String extractChatId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                if ("chatId".equalsIgnoreCase(key)) {
                    return pair.substring(idx + 1);
                }
            }
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }
        try {
            JSONObject root = new JSONObject(payload);
            String type = root.optString("type", "");
            if ("ping".equalsIgnoreCase(type)) {
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                return;
            }
            if ("sendChat".equalsIgnoreCase(type)) {
                String chatId = root.optString("chatId", "");
                String chatMessage = root.optString("message", "");
                String tempId = root.optString("tempId", "");
                log.info("Received sendChat message: chatId={}, message={}", chatId, chatMessage);
                String fromUserId = sessionRegistry.getUserIdBySessionId(session.getId());
                if (!StringUtils.hasText(chatId) || !StringUtils.hasText(fromUserId)) {
                    log.warn("sendChat missing chatId or userId: chatId={}, sessionId={}", chatId, session.getId());
                    return;
                }
                String key = CHAT_USERS_KEY_PREFIX + chatId + CHAT_USERS_KEY_SUFFIX;
                Set<String> users = stringRedisTemplate.opsForSet().members(key);
                if (users == null || users.isEmpty()) {
                    log.warn("sendChat no users found for chatId: chatId={}, key={}", chatId, key);
                    return;
                }
                  
                String messageId = UuidCreator.getTimeOrderedEpoch().toString();
                messageDao.insertMessage(messageId, chatId, fromUserId, chatMessage);
                JSONObject ack = new JSONObject();
                ack.put("type", "sendChatAck");
                ack.put("status", "success");
                ack.put("tempId", tempId);
                ack.put("messageId", messageId);
                ack.put("chatId", chatId);
                session.sendMessage(new TextMessage(ack.toString()));

                for (String userId : users) {//chat:ITs7P:users 
                    if (!StringUtils.hasText(userId) || userId.equals(fromUserId)) {
                        continue;
                    }
                    log.info("sendChat chatId={}, fromUserId={}, potential toUserId={}", chatId, fromUserId, userId);
                    JSONObject outbound = new JSONObject();
                    outbound.put("type", "CHAT_MESSAGE");
                    outbound.put("messageId", messageId);
                    outbound.put("chatId", chatId);
                    outbound.put("fromUserId", fromUserId);
                    outbound.put("toUserId", userId);
                    outbound.put("message", chatMessage);
                    log.info("sendChat published: chatId={}, fromUserId={}, toUserId={}", chatId, fromUserId, userId);
                    stringRedisTemplate.convertAndSend("chat:" + chatId, outbound.toString());
                    log.info("Generated messageId={}, chatId={}, fromUserId={}, toUserId={}", messageId, chatId, fromUserId, userId);
                }
                return;
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message: sessionId={}, payload={}",
                    session.getId(), payload, e);
        }
    }

    private void notifyUserJoined(String chatId, String userId) {
        try {
            String key = CHAT_USERS_KEY_PREFIX + chatId + CHAT_USERS_KEY_SUFFIX;
            Set<String> users = stringRedisTemplate.opsForSet().members(key);
            if (users == null || users.isEmpty()) {
                log.info("userJoined skipped: no users in Redis for chatId={}", chatId);
                return;
            }
            log.info("userJoined users in Redis: chatId={}, users={}", chatId, users);
            JSONObject outbound = new JSONObject();
            outbound.put("type", "userJoined");
            outbound.put("chatId", chatId);
            outbound.put("userId", userId);
            for (String targetUserId : users) {
                if (!StringUtils.hasText(targetUserId) || targetUserId.equals(userId)) {
                    continue;
                }
                WebSocketSession target = sessionRegistry.getSessionByUserId(targetUserId);
                if (target != null && target.isOpen()) {
                    target.sendMessage(new TextMessage(outbound.toString()));
                    log.info("userJoined sent: chatId={}, userId={}, toUserId={}", chatId, userId, targetUserId);
                } else {
                    log.warn("No active session for userId={} (userJoined)", targetUserId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to publish userJoined: chatId={}, userId={}", chatId, userId, e);
        }
    }

    private void removeRedisSession(String userId) {
        try {
            String userKey = "user:" + userId + ":chat";
            String chatId = stringRedisTemplate.opsForValue().get(userKey);
            stringRedisTemplate.delete(userKey);
            if (chatId != null) {
                stringRedisTemplate.opsForSet().remove(CHAT_USERS_KEY_PREFIX + chatId + CHAT_USERS_KEY_SUFFIX, userId);
            }
            log.info("Removed Redis session for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to remove Redis session for userId={}", userId, e);
        }
    }
}
