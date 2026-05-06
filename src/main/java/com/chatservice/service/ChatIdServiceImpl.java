package com.chatservice.service;

import com.chatservice.dao.ChatUserDao;


import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ChatIdServiceImpl implements ChatIdService {

    private static final Logger log = LoggerFactory.getLogger(ChatIdServiceImpl.class);
    private static final char[] ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int CHAT_ID_LENGTH = 5;
    private static final SecureRandom random = new SecureRandom();
    private static final String CHAT_USERS_KEY_PREFIX = "chat:";
    private static final String CHAT_USERS_KEY_SUFFIX = ":users";

    private final SetOperations<String, String> setOps;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChatUserDao chatUserDao;

    public ChatIdServiceImpl(StringRedisTemplate stringRedisTemplate, ChatUserDao chatUserDao) {
        this.setOps = stringRedisTemplate.opsForSet();
        this.stringRedisTemplate = stringRedisTemplate;
        this.chatUserDao = chatUserDao;
    }

    @Override
    public Map<String, String> generateChatId(String userId) {

        try{
            if (userId == null || userId.isBlank()) {
                return Map.of("status", "error", "messageCode", "CS003");
            }
            StringBuilder sb = new StringBuilder(CHAT_ID_LENGTH);
            for (int i = 0; i < CHAT_ID_LENGTH; i++) {
                sb.append(ALPHANUM[random.nextInt(ALPHANUM.length)]);
            }
            String chatId = sb.toString();
            String key = CHAT_USERS_KEY_PREFIX + chatId + CHAT_USERS_KEY_SUFFIX;
            setOps.add(key, userId);
            stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);
            chatUserDao.addUserToChat(chatId, userId);
            
            String userKey = "user:" + userId + ":chat";
            stringRedisTemplate.opsForValue().set(userKey, chatId, 30, TimeUnit.MINUTES);

            log.info("Created chatId and added user to Redis: chatId={}, userId={}, key={}", chatId, userId, key);
            return Map.of("status", "ok", "chatId", chatId);
        } catch (Exception e) {
            log.error("Error generating chatId for userId={}: {}", userId, e.getMessage());
        }
        return Map.of("status", "error", "messageCode", "CS004");
    }

    @Override
    public Map<String, String> joinChat(String chatId, String userId) {
        try{
            if (userId == null || userId.isBlank()) {
                return Map.of("status", "error", "messageCode", "CS003");
            }
            
            if(chatId == null || chatId.isBlank()) {
               return Map.of("status", "error", "messageCode", "CS001"); 
            }
            
            String key = CHAT_USERS_KEY_PREFIX + chatId + CHAT_USERS_KEY_SUFFIX;
            Boolean exists = stringRedisTemplate.hasKey(key);
            if (exists == null || !exists) {
                log.warn("Join chat failed: chatId not found: chatId={}, userId={}, key={}", chatId, userId, key);
                return Map.of("status", "error", "messageCode", "CS001");
            }

            if(isUserInChat(userId , chatId)){
                log.warn("Join chat failed: user already in a chat: userId={}", userId);
                return Map.of("status", "error", "messageCode", "CS006");
            }
            
            if(setOps.size(key) >= 3){
                log.warn("Join chat failed: chatId full: chatId={}, userId={}, key={}", chatId, userId, key);
                return Map.of("status", "error", "messageCode", "CS005");
            }
            setOps.add(key, userId);
            chatUserDao.addUserToChat(chatId, userId);

            String userKey = "user:" + userId + ":chat";
            stringRedisTemplate.opsForValue().set(userKey, chatId, 30, TimeUnit.MINUTES);
            
            log.info("User connected to chatId={}, current users={}", chatId, setOps.members(key));
        
            return Map.of("status", "ok");
        } catch(Exception e){
            log.error("Error joining chatId={} for userId={}: {}", chatId, userId, e.getMessage());
            return Map.of("status", "error", "messageCode", "CS004");
        }
    }

    private boolean isUserInChat(String userId, String chatId){
        String userKey = "user:" + userId + ":chat";
        String existingChatid =  stringRedisTemplate.opsForValue().get(userKey);
        return existingChatid != null &&  !existingChatid.equals(chatId) ;
    }
}
