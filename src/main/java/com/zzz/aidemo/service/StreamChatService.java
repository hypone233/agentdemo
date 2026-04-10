package com.zzz.aidemo.service;

import com.zzz.aidemo.dto.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface StreamChatService {

    public SseEmitter streamReply(ChatRequest chatRequest);

}
