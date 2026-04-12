package com.zzz.aidemo.agent.model;

public class AgentStepResult {

    /**
     * 当前步骤输出给外部看的内容。
     */
    private final String content;

    /**
     * 当前步骤执行完后，是否继续下一步。
     */
    private final boolean continueNextStep;

    /**
     * 当前步骤是否已经明确结束任务。
     */
    private final boolean finished;

    private AgentStepResult(String content, boolean continueNextStep, boolean finished) {
        this.content = content;
        this.continueNextStep = continueNextStep;
        this.finished = finished;
    }

    /**
     * 普通继续执行的步骤结果。
     */
    public static AgentStepResult next(String content) {
        return new AgentStepResult(content, true, false);
    }

    /**
     * 明确结束的步骤结果。
     */
    public static AgentStepResult finish(String content) {
        return new AgentStepResult(content, false, true);
    }

    public String getContent() {
        return content;
    }

    public boolean isContinueNextStep() {
        return continueNextStep;
    }

    public boolean isFinished() {
        return finished;
    }


}
