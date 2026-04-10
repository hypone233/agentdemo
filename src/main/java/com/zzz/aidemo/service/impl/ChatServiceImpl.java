package com.zzz.aidemo.service.impl;

import com.zzz.aidemo.dto.ChatRequest;
import com.zzz.aidemo.entity.ChatMessage;
import com.zzz.aidemo.memory.ChatMemoryStore;
import com.zzz.aidemo.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ChatMemoryStore chatMemoryStore;

    @Override
    public String processChat(ChatRequest chatRequest) {

        //第一版（没有实现记忆）
        /*if (message.length() > 10000) {
            throw new IllegalArgumentException("消息长度不能超过10000个字符");
        }
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("模型返回内容为空");
        }

        return content;*/

        String chatId = chatRequest.getChatId();
        String userMessage = chatRequest.getUserMessage();

        List<ChatMessage> history = chatMemoryStore.getMessage(chatId);

        history.add(new ChatMessage("user",userMessage));

        List<Message> messages = new ArrayList<>();
        for (ChatMessage message : history) {
            if ("user".equals(message.getRole())) {
                messages.add(new UserMessage(message.getContent()));
            } else if ("assistant".equals(message.getRole())) {
                messages.add(new AssistantMessage(message.getContent()));
            }
        }

        String content = chatClient.prompt()
                        .messages(messages)
                        .call()
                        .content();

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("模型返回内容为空");
        }

        history.add(new ChatMessage("assistant",content));
        return content;
    }
}
