package com.zzz.aidemo.agent.model;

public class AgentEvent {

    /**
     * 事件类型，比如开始、步骤结果、结束、异常。
     */
    private final AgentEventType type;

    /**
     * 当前步骤号。
     * 开始和结束事件可以为 0。
     */
    private final int step;

    /**
     * 事件内容。
     */
    private final String message;

    public AgentEvent(AgentEventType type, int step, String message) {
        this.type = type;
        this.step = step;
        this.message = message;
    }

    public AgentEventType getType() {
        return type;
    }

    public int getStep() {
        return step;
    }

    public String getMessage() {
        return message;
    }

}
