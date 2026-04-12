package com.zzz.aidemo.agent;

import com.zzz.aidemo.advisor.MyLoggerAdvisor;
import com.zzz.aidemo.advisor.MySafeGuardAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
}