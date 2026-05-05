package com.chatservice.dao;

import java.util.List;

import com.chatservice.api.MessageResponse;

public interface MessageDao {
    void insertMessage(String messageId, String chatId, String senderId, String content);

    List<MessageResponse> getMessages(String chatId, String lastMessageId, Integer limit);
}
