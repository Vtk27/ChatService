package com.chatservice.service;

import com.chatservice.api.MessageResponse;
import com.chatservice.dao.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private static final String CHAT_USERS_KEY_PREFIX = "chat:";
    private static final String CHAT_USERS_KEY_SUFFIX = ":users";

    private final MessageDao messageDao;
    private final StringRedisTemplate stringRedisTemplate;

    public MessageServiceImpl(MessageDao messageDao, StringRedisTemplate stringRedisTemplate) {
        this.messageDao = messageDao;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<MessageResponse> getMessages(String chatId, String lastMessageId, Integer limit) {
        String trimmedChatId = chatId == null ? "" : chatId.trim();
        if (trimmedChatId.isEmpty()) {
            return Collections.emptyList();
        }

        String key = CHAT_USERS_KEY_PREFIX + trimmedChatId + CHAT_USERS_KEY_SUFFIX;
        Boolean exists = stringRedisTemplate.hasKey(key);
        if (exists == null || !exists) {
            log.info("Rejecting message history request for expired/unknown chatId={}", trimmedChatId);
            return Collections.emptyList();
        }

        return messageDao.getMessages(trimmedChatId, lastMessageId, limit);
    }
}
