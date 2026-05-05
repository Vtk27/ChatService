package com.chatservice.dao;

import java.util.List;

public interface ChatUserDao {

    void addUserToChat(String chatId, String userId);

    void removeUserFromChat(String chatId, String userId);

    List<String> getUsersForChat(String chatId);
}
