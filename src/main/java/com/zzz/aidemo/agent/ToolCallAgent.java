package com.zzz.aidemo.agent;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class ToolCallAgent extends ReActAgent {

    private static final Logger logger = LoggerFactory.getLogger(ToolCallAgent.class);


    /**
     * 当前 Agent 可调用的工具。
     */
    private final ToolCallback[] availableTools;

    /**
     * 保存 think 阶段的模型响应。
     * act 阶段会从这里读取工具调用信息。
     */
    private ChatResponse toolCallChatResponse;

    /**
     * 工具执行管理器。
     */
    private final ToolCallingManager toolCallingManager;

    /**
     * 工具调用配置。
     *
     * 这里关闭 Spring AI 自动执行工具，
     */
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        try {
            // 如果配置了 nextStepPrompt，就把它作为补充提示词塞进上下文
            if (StrUtil.isNotBlank(getNextStepPrompt())) {
                getMessageList().add(new UserMessage(getNextStepPrompt()));
            }

            List<Message> messageList = getMessageList();
            Prompt prompt = new Prompt(messageList, this.chatOptions);

            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();

            this.toolCallChatResponse = chatResponse;

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            logger.info("[{}] 模型思考结果: {}", getName(), assistantMessage.getText());
            logger.info("[{}] 本轮选择工具数: {}", getName(), toolCallList.size());

            // 没有工具调用，说明模型认为当前可以直接结束
            if (toolCallList == null || toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("[{}] think 阶段失败", getName(), e);
            getMessageList().add(new AssistantMessage("处理过程中出现错误：" + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }

        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult =
                toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // 工具执行后，conversationHistory 会包含更新后的对话上下文
        setMessageList(toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 如果调用了 terminate 工具，就结束 Agent
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));

        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }

        return toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
    }
}
