package com.zzz.aidemo.service.impl;

import com.zzz.aidemo.service.StreamChatService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class StreamChatServiceImpl implements StreamChatService {
    @Override
    public SseEmitter streamReply(String message) {
        SseEmitter sseEmitter = new SseEmitter(60_000L);

        sseEmitter.onTimeout(()->{
            System.out.println("SSE 连接超时");
            sseEmitter.complete();
        });

        sseEmitter.onCompletion(()->{
            System.out.println("SSE 连接结束");
        });

        new Thread(()->{
            try{
                String[] parts = {
                        "收到你的消息：" + message,
                        "我先简单分析一下...",
                        "这是第二段模拟回复。",
                        "本次流式输出结束。"
                };
                for(String part: parts){
                    sseEmitter.send(part);
                    Thread.sleep(300);
                }
                sseEmitter.complete();
            }catch (Exception e){
                sseEmitter.completeWithError(e);
            }
        }).start();
        return sseEmitter;
    }

}
