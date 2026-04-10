package com.zzz.aidemo.service.impl;

import com.zzz.aidemo.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatClient chatClient;

    @Override
    public String processChat(String message) {

        
        if (message.length() > 10000) {
            throw new IllegalArgumentException("消息长度不能超过10000个字符");
        }
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("模型返回内容为空");
        }

        return content;
    }
}
