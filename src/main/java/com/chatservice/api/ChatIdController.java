package com.chatservice.api;

import com.chatservice.global.Messages;
import com.chatservice.service.ChatIdService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatIdController {

    private final ChatIdService chatIdService;

    public ChatIdController(ChatIdService chatIdService) {
        this.chatIdService = chatIdService;
    }

    @PostMapping("/chat-id")
    public ResponseEntity<Map<String, String>> createChatId(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
       
        Map<String, String> result = chatIdService.generateChatId(userId);
        if (result.get("status").equals("error")) {
            return errorBody(result.get("messageCode"));
        }
        return ResponseEntity.ok(Map.of("chatId", result.get("chatId")));
    }

    @GetMapping("/chat/join")
    public ResponseEntity<Map<String, String>> joinChat(@RequestParam("chatId") String chatId,
                                           @RequestParam("userId") String userId) {
        Map<String, String> result = chatIdService.joinChat(chatId, userId);
        if (result.get("status").equals("error")) {
            return errorBody(result.get("messageCode"));
        }
        return ResponseEntity.ok(Map.of("message", "Successfully joined the chat."));
    }

    @GetMapping("/health")
    public String cheakHealth(){
        return "OK";
    }
    private ResponseEntity<Map<String, String>> errorBody(String messageCode) {
        Messages message = Messages.valueOf(messageCode);
        return ResponseEntity.status(message.getStatusCode()).body(Map.of("message", message.getMessage()));
    }
}
