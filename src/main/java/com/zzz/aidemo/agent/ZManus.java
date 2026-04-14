package com.zzz.aidemo.agent;

import com.zzz.aidemo.advisor.MyLoggerAdvisor;
import com.zzz.aidemo.advisor.MySafeGuardAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ZManus extends ToolCallAgent {

    @Autowired
    public ZManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);

        setName("zManus");
        setMaxSteps(10);

        setSystemPrompt("""
                You are ZManus, an AI assistant that can solve user tasks with tools.
                Keep responses clear and structured.
                """);

        setNextStepPrompt("""
                Based on the user request, decide whether to use tools.
                If no tool is needed, give the final answer directly.
                If the task is complete, stop the interaction.
                """);

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new MyLoggerAdvisor(),
                        MySafeGuardAdvisor.builder()
                                .defaultSensitiveWords()
                                .build()
                )
                .build();

        setChatClient(chatClient);
    }

   /* private final Map<String, List<Message>> conversationStore = new ConcurrentHashMap<>();

    public String run(String userPrompt, String chatId) {
        String conversationId = normalizeChatId(chatId);

        // 1. 读取历史，复制到当前运行上下文
        List<Message> history = conversationStore.getOrDefault(conversationId, new ArrayList<>());
        setMessageList(new ArrayList<>(history));

        // 2. 调用父类执行
        String result = super.run(userPrompt);

        // 3. 保存最新上下文
        conversationStore.put(conversationId, new ArrayList<>(getMessageList()));

        return result;
    }
    public SseEmitter runStream(String userPrompt, String chatId) {
        String conversationId = normalizeChatId(chatId);

        List<Message> history = conversationStore.getOrDefault(conversationId, new ArrayList<>());
        setMessageList(new ArrayList<>(history));

        SseEmitter sseEmitter = super.runStream(userPrompt);

        return sseEmitter;
    }*/



    /*private String normalizeChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return chatId.trim();
    }*/

}