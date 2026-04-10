package com.zzz.aidemo.service.impl;

import com.zzz.aidemo.service.StreamChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@Service
public class StreamChatServiceImpl implements StreamChatService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private View error;


    @Override
    public SseEmitter streamReply(String message) {
        SseEmitter sseEmitter = new SseEmitter(60_000L);

        Disposable subscription = chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .subscribe(
                        content -> {
                            try {
                                sseEmitter.send(content);
                            } catch (Exception ex) {
                                sseEmitter.completeWithError(ex);
                            }
                        },
                        error -> sseEmitter.completeWithError(error),
                        sseEmitter::complete
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
