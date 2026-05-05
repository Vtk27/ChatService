package com.chatservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.chatservice.api.MessageResponse;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Repository
public class MessageDaoImpl implements MessageDao {

    private static final Logger log = LoggerFactory.getLogger(MessageDaoImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public MessageDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertMessage(String messageId, String chatId, String senderId, String content) {
        try {
            jdbcTemplate.update(PreparedStatements.INSERT_MESSAGE, messageId, chatId, senderId, content);
        } catch (Exception e) {
            log.error("Failed to insert message: messageId={}, chatId={}, senderId={}", messageId, chatId, senderId, e);
            return ;
        }
    }

    @Override
    public List<MessageResponse> getMessages(String chatId, String lastMessageId, Integer limit) {
        String trimmedChatId = chatId == null ? "" : chatId.trim();
        String trimmedLastId = lastMessageId == null ? "" : lastMessageId.trim();
        if (trimmedChatId.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            if (trimmedLastId.isEmpty()) {
                if (limit != null && limit > 0) {
                    List<MessageResponse> limited = jdbcTemplate.query(
                            PreparedStatements.SELECT_MESSAGES_BY_CHAT_ID_LIMIT,
                            (rs, rowNum) -> {
                                Timestamp ts = rs.getTimestamp("created_at");
                                long createdAt = ts == null ? 0L : ts.toInstant().toEpochMilli();
                                return new MessageResponse(
                                        rs.getString("message_id"),
                                        rs.getString("chat_id"),
                                        rs.getString("sender_id"),
                                        rs.getString("content"),
                                        createdAt
                                );
                            },
                            trimmedChatId,
                            limit
                    );

                    Collections.reverse(limited);
                    return limited;
                }

                return jdbcTemplate.query(
                        PreparedStatements.SELECT_MESSAGES_BY_CHAT_ID,
                        (rs, rowNum) -> {
                            Timestamp ts = rs.getTimestamp("created_at");
                            long createdAt = ts == null ? 0L : ts.toInstant().toEpochMilli();
                            return new MessageResponse(
                                    rs.getString("message_id"),
                                    rs.getString("chat_id"),
                                    rs.getString("sender_id"),
                                    rs.getString("content"),
                                    createdAt
                            );
                        },
                        trimmedChatId
                );
            }

            List<MessageResponse> messages = jdbcTemplate.query(
                    PreparedStatements.SELECT_MESSAGES_BY_CHAT_ID_BEFORE,
                    (rs, rowNum) -> {
                        Timestamp ts = rs.getTimestamp("created_at");
                        long createdAt = ts == null ? 0L : ts.toInstant().toEpochMilli();
                        return new MessageResponse(
                                rs.getString("message_id"),
                                rs.getString("chat_id"),
                                rs.getString("sender_id"),
                                rs.getString("content"),
                                createdAt
                        );
                    },
                    trimmedChatId,
                    trimmedLastId,
                    limit
            );
            Collections.reverse(messages);
            return messages;
        } catch (Exception e) {
            log.error("Failed to fetch messages: chatId={}, lastMessageId={}, limit={}", chatId, lastMessageId, limit, e);
            return Collections.emptyList();
        }
    }
}
