package com.chatservice.api;

import com.chatservice.dao.MessageDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final MessageDao messageDao;

    public MessageController(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @GetMapping("/messages")
    public List<MessageResponse> getMessages(
            @RequestParam("chatId") String chatId,
            @RequestParam(value = "lastMessageId", required = false) String lastMessageId,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return messageDao.getMessages(chatId, lastMessageId, limit);
    }
}
