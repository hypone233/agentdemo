package com.zzz.aidemo.service.impl;

import com.zzz.aidemo.dto.ChatRequest;
import com.zzz.aidemo.entity.ChatMessage;
import com.zzz.aidemo.memory.ChatMemoryStore;
import com.zzz.aidemo.service.StreamChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;

@Service
public class StreamChatServiceImpl implements StreamChatService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ChatMemoryStore chatMemoryStore;



    @Override
    public SseEmitter streamReply(ChatRequest chatRequest) {

        String chatId = chatRequest.getChatId();
        String userMessage = chatRequest.getUserMessage();

        List<ChatMessage> history = chatMemoryStore.getMessage(chatId);

        history.add(new ChatMessage("user", userMessage));

        List<Message> messages = new ArrayList<>();
        for (ChatMessage message : history) {
            if ("user".equals(message.getRole())) {
                messages.add(new UserMessage(message.getContent()));
            } else if ("assistant".equals(message.getRole())) {
                messages.add(new AssistantMessage(message.getContent()));
            }
        }

        SseEmitter sseEmitter = new SseEmitter(60_000L);

        StringBuilder fullReply = new StringBuilder();

        Disposable subscription = chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .subscribe(
                        chunk -> {
                            try {
                                fullReply.append(chunk);
                                sseEmitter.send(chunk);
                            } catch (Exception ex) {
                                sseEmitter.completeWithError(ex);
                            }
                        },
                        error -> sseEmitter.completeWithError(error),
                        () -> {
                            // 4. 流结束后再保存完整 assistant 回复
                            String assistantReply = fullReply.toString();
                            if (!assistantReply.isBlank()) {
                                history.add(new ChatMessage("assistant", assistantReply));
                            }
                            sseEmitter.complete();
                        }
                );
        sseEmitter.onTimeout(() -> {
            subscription.dispose();
            sseEmitter.complete();
        });
        sseEmitter.onCompletion(subscription::dispose);

        sseEmitter.onError(error -> subscription.dispose());

        return sseEmitter;

    }

}
