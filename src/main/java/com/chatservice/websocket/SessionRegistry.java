package com.chatservice.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    public void addSession(WebSocketSession session, String userId) {
        sessions.put(session.getId(), session);
        if (userId != null && !userId.isBlank()) {
            userSessions.put(userId, session);
            sessionUsers.put(session.getId(), userId);
        }
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        String userId = sessionUsers.remove(session.getId());
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    public WebSocketSession getSessionByUserId(String userId) {
        return userSessions.get(userId);
    }

    public String getUserIdBySessionId(String sessionId) {
        return sessionUsers.get(sessionId);
    }
}
