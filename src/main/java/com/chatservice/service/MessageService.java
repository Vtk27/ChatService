package com.chatservice.service;

import com.chatservice.api.MessageResponse;

import java.util.List;

public interface MessageService {
    List<MessageResponse> getMessages(String chatId, String lastMessageId, Integer limit);
}
