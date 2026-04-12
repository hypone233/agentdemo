package com.zzz.aidemo.agent;

public class DemoAgent extends BaseAgent{

    public DemoAgent(String userMessage) {
        // 这里只允许最多执行 3 步
        super(userMessage, 3);
    }

    @Override
    protected String step() {
        return switch (getCurrentStep()) {
            case 1 -> "分析用户问题： " + getUserMessage();
            case 2 -> "拆解执行计划：先理解目标，再规划执行步骤";
            case 3 -> "生成最终回答：这是最小 Agent 骨架的演示结果";
            default -> "没有更多步骤";
        };
    }
    @Override
    protected boolean shouldFinish(String stepResult) {
        // 这里直接按步数结束，是最简单最清晰的结束条件
        return getCurrentStep() >= 3;
    }
}
