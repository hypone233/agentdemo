package com.zzz.aidemo.service.impl;

import com.zzz.aidemo.agent.ZManus;
import com.zzz.aidemo.service.ManusService;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Data
public class ManusServiceImpl implements ManusService {
    private final ToolCallback[] allTools;
    private final ChatModel dashscopeChatModel;

    private final Map<String, List<Message>> conversationStore = new ConcurrentHashMap<>();

    public ManusServiceImpl(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        this.allTools = allTools;
        this.dashscopeChatModel = dashscopeChatModel;
    }


    @Override
    public String run(String userPrompt, String chatId) {
        String conversationId = normalizeChatId(chatId);
        //读历史
        List<Message> history = conversationStore.getOrDefault(conversationId,new ArrayList<>());

        ZManus zManus = createAgent();

        zManus.setMessageList(new ArrayList<>(history));

        String result = zManus.run(userPrompt);

        conversationStore.put(conversationId, new ArrayList<>(zManus.getMessageList()));

        return result;
    }

    public SseEmitter runStream(String userPrompt, String chatId){
        String conversationId = normalizeChatId(chatId);
        List<Message> history = conversationStore.getOrDefault(conversationId, new ArrayList<>());

        ZManus zManus = createAgent();
        zManus.setMessageList(new ArrayList<>(history));

        zManus.setBeforeCleanupHook(() ->
                conversationStore.put(conversationId, new ArrayList<>(zManus.getMessageList())));

        SseEmitter sseEmitter = zManus.runStream(userPrompt);

        return sseEmitter;
    }


    private ZManus createAgent() {
        return new ZManus(allTools, dashscopeChatModel);
    }

    /**
     * 归一化 chatId。
     * 如果外部没传，就生成一个新的会话 ID。
     */
    private String normalizeChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return chatId.trim();
    }
}
