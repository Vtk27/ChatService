package com.chatservice.api;

import com.chatservice.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    public List<MessageResponse> getMessages(
            @RequestParam("chatId") String chatId,
            @RequestParam(value = "lastMessageId", required = false) String lastMessageId,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return messageService.getMessages(chatId, lastMessageId, limit);
    }
}
