package com.zzz.aidemo.controller;

import com.zzz.aidemo.dto.ChatRequest;
import com.zzz.aidemo.dto.ChatResponse;
import com.zzz.aidemo.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/sync")
    public ChatResponse chatResponse(@Valid @RequestBody ChatRequest chatRequest){
        String result = chatService.processChat(chatRequest);
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setContent(result);
        return chatResponse;
    }

}
