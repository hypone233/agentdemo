package com.zzz.aidemo.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ManusService {

    String run(String userPrompt, String chatId);

    SseEmitter runStream(String userPrompt, String chatId);

}
