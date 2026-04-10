package com.zzz.aidemo.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface StreamChatService {

    public SseEmitter streamReply(String message);

}
