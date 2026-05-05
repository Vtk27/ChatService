package com.chatservice.service;

import java.util.Map;

public interface ChatIdService {
    Map<String, String> generateChatId(String userId);
    Map<String, String> joinChat(String chatId, String userId);
}
