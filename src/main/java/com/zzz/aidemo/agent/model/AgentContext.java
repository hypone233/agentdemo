package com.zzz.aidemo.agent.model;

import com.zzz.aidemo.agent.AgentState;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentContext {

    /**
     * 当前 Agent 状态。
     */
    private AgentState state = AgentState.IDLE;

    /**
     * 当前执行到第几步。
     */
    private int currentStep = 0;

    /**
     * 最大允许执行步数。
     */
    private int maxSteps = 10;

    /**
     * 会话消息上下文。
     */
    private List<Message> messageList = new ArrayList<>();

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    /**
     * 重置运行态，但保留结构本身。
     * 后面 cleanup 后可以回到一个干净状态。
     */
    public void reset() {
        this.state = AgentState.IDLE;
        this.currentStep = 0;
        this.messageList = new ArrayList<>();
    }

}
