package com.chatservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatUserDaoImpl implements ChatUserDao {

    private static final Logger log = LoggerFactory.getLogger(ChatUserDaoImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ChatUserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addUserToChat(String chatId, String userId) {
        try {
            jdbcTemplate.update(PreparedStatements.INSERT_CHAT_USER, chatId, userId);
        } catch (Exception e) {
            log.error("Failed to add user to chat: chatId={}, userId={}", chatId, userId, e);
            throw e;
        }
    }

    @Override
    public void removeUserFromChat(String chatId, String userId) {
        try {
            jdbcTemplate.update(PreparedStatements.DELETE_CHAT_USER, chatId, userId);
        } catch (Exception e) {
            log.error("Failed to remove user from chat: chatId={}, userId={}", chatId, userId, e);
            throw e;
        }
    }

    @Override
    public List<String> getUsersForChat(String chatId) {
        try {
            return jdbcTemplate.query(PreparedStatements.SELECT_CHAT_USERS_BY_CHAT_ID,
                    (rs, rowNum) -> rs.getString("user_id"), chatId);
        } catch (Exception e) {
            log.error("Failed to fetch users for chat: chatId={}", chatId, e);
            throw e;
        }
    }
}
