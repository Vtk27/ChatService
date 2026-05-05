package com.chatservice.dao;

public final class PreparedStatements {

    private PreparedStatements() {}

    public static final String INSERT_CHAT_USER =
            "INSERT INTO chat_users (chat_id, user_id) VALUES (?, ?) ON CONFLICT (chat_id, user_id) DO NOTHING";

    public static final String DELETE_CHAT_USER =
            "DELETE FROM chat_users WHERE chat_id = ? AND user_id = ?";

    public static final String SELECT_CHAT_USERS_BY_CHAT_ID =
            "SELECT user_id FROM chat_users WHERE chat_id = ?";

    public static final String INSERT_MESSAGE =
            "INSERT INTO messages (message_id, chat_id, sender_id, content) VALUES (?::uuid, ?, ?, ?)";

    public static final String SELECT_MESSAGES_BY_CHAT_ID =
            "SELECT message_id, chat_id, sender_id, content, created_at FROM messages WHERE chat_id = ? ORDER BY created_at ASC";

    public static final String SELECT_MESSAGES_BY_CHAT_ID_BEFORE =
            "SELECT message_id, chat_id, sender_id, content, created_at FROM messages WHERE chat_id = ? AND message_id < ?::uuid ORDER BY message_id DESC LIMIT ?";

    public static final String SELECT_MESSAGES_BY_CHAT_ID_LIMIT =
            "SELECT message_id, chat_id, sender_id, content, created_at FROM messages WHERE chat_id = ? ORDER BY message_id DESC LIMIT ?";
}
