package com.zzz.aidemo.controller;

import com.zzz.aidemo.dto.ChatRequest;
import com.zzz.aidemo.dto.ChatResponse;
import com.zzz.aidemo.service.StreamChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat")
public class StreamChatController {

    @Autowired
    private StreamChatService streamChatService;

    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody @Valid ChatRequest chatRequest){

        SseEmitter sseEmitter = streamChatService.streamReply(chatRequest.getUserMessage());
        return sseEmitter;
    }
}
