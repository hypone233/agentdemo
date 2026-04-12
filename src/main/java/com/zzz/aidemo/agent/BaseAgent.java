package com.zzz.aidemo.agent;

import cn.hutool.core.util.StrUtil;
import com.zzz.aidemo.agent.model.AgentContext;
import com.zzz.aidemo.agent.model.AgentEvent;
import com.zzz.aidemo.agent.model.AgentEventType;
import com.zzz.aidemo.agent.model.AgentStepResult;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
public abstract class BaseAgent {

    private static final Logger logger = LoggerFactory.getLogger(BaseAgent.class);


    /**
     * Agent 基本配置。
     * 这些还是属于 Agent 自身，而不是运行上下文。
     */
    private String name;
    private String systemPrompt;
    private String nextStepPrompt;
    private ChatClient chatClient;

    /**
     * 关键点，把运行时数据隔离
     */
    private final AgentContext context = new AgentContext();

    /**
     * 暴露只读式上下文访问入口。
     * 这样子类仍然能用，但运行态已经被集中管理。
     */
    public AgentContext getContext() {
        return context;
    }

    public AgentState getState() {
        return context.getState();
    }

    public void setState(AgentState state) {
        context.setState(state);
    }

    public int getCurrentStep() {
        return context.getCurrentStep();
    }

    public void setCurrentStep(int currentStep) {
        context.setCurrentStep(currentStep);
    }

    public int getMaxSteps() {
        return context.getMaxSteps();
    }

    public void setMaxSteps(int maxSteps) {
        context.setMaxSteps(maxSteps);
    }

    public List<Message> getMessageList() {
        return context.getMessageList();
    }

    public void setMessageList(List<Message> messageList) {
        context.setMessageList(messageList);
    }




    public String run(String userPrompt){
        validateBeforeRun(userPrompt);
        initializeRun(userPrompt);

        List<String> results = new ArrayList<>();

        try {
            while(canContinue()){

                int stepNumber = getCurrentStep() + 1;
                setCurrentStep(stepNumber);
                logger.info("Executing step {}/{}", stepNumber, getMaxSteps());
                AgentStepResult stepResult = step();
                results.add("Step " + stepNumber + ": " + stepResult.getContent());

                if (stepResult.isFinished() || !stepResult.isContinueNextStep()) {
                    setState(AgentState.FINISHED);
                }
            }
            if (getCurrentStep() >= getMaxSteps() && getState() != AgentState.FINISHED) {
                setState(AgentState.FINISHED);
                results.add("Terminated: Reached max steps (" + getMaxSteps() + ")");
            }
            return String.join("\n", results);

        }catch (Exception e){
            setState(AgentState.ERROR);
            logger.error("error executing agent", e);
            return "执行错误：" + e.getMessage();
        }finally {
            cleanup();
        }

    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                validateBeforeRun(userPrompt);
                initializeRun(userPrompt);

                sendEvent(sseEmitter, new AgentEvent(AgentEventType.START, 0, "Agent 开始执行"));

                while (canContinue()) {
                    int stepNumber = getCurrentStep() + 1;
                    setCurrentStep(stepNumber);

                    sendEvent(sseEmitter, new AgentEvent(AgentEventType.STEP_START, stepNumber,
                            "开始第 " + stepNumber + " 步"));

                    AgentStepResult stepResult = step();

                    sendEvent(sseEmitter, new AgentEvent(AgentEventType.STEP_RESULT, stepNumber,
                            stepResult.getContent()));

                    if (stepResult.isFinished() || !stepResult.isContinueNextStep()) {
                        setState(AgentState.FINISHED);
                    }
                }

                if (getCurrentStep() >= getMaxSteps() && getState() != AgentState.FINISHED) {
                    setState(AgentState.FINISHED);
                    sendEvent(sseEmitter, new AgentEvent(AgentEventType.FINISH, getCurrentStep(),
                            "执行结束：达到最大步骤（" + getMaxSteps() + "）"));
                } else {
                    sendEvent(sseEmitter, new AgentEvent(AgentEventType.FINISH, getCurrentStep(),
                            "Agent 执行完成"));
                }

                sseEmitter.complete();
            } catch (Exception e) {
                setState(AgentState.ERROR);
                logger.error("error executing agent", e);
                sendErrorSafely(sseEmitter, e);
                sseEmitter.completeWithError(e);
            } finally {
                cleanup();
            }
        });

        sseEmitter.onTimeout(() -> {
            setState(AgentState.ERROR);
            cleanup();
            logger.warn("SSE connection timeout");
        });

        sseEmitter.onCompletion(() -> {
            if (getState() == AgentState.RUNNING) {
                setState(AgentState.FINISHED);
            }
            cleanup();
            logger.info("SSE connection completed");
        });

        return sseEmitter;
    }

    protected abstract AgentStepResult step();

    /**
     * 执行前校验。
     * 把校验逻辑单独抽出来，run 和 runStream 共用。
     */
    protected void validateBeforeRun(String userPrompt) {
        if (getState() != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + getState());
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
    }
    /**
     * 执行前初始化。
     * 统一设置状态、步数和消息上下文。
     */
    protected void initializeRun(String userPrompt) {
        setState(AgentState.RUNNING);
        setCurrentStep(0);
        getMessageList().add(new UserMessage(userPrompt));
    }

    /**
     * 是否还能继续下一轮。
     */
    protected boolean canContinue() {
        return getCurrentStep() < getMaxSteps() && getState() != AgentState.FINISHED;
    }

    /**
     * 发送结构化事件。
     */
    protected void sendEvent(SseEmitter sseEmitter, AgentEvent event) throws IOException {
        sseEmitter.send(SseEmitter.event()
                .name(event.getType().name())
                .data(event));
    }

    protected void sendErrorSafely(SseEmitter emitter, Exception exception) {
        try {
            sendEvent(emitter, new AgentEvent(AgentEventType.ERROR, getCurrentStep(),
                    "执行错误：" + exception.getMessage()));
        } catch (IOException ignored) {
        }
    }

    /**
     * 清理资源。
     */
    protected void cleanup() {

        if (getState() == AgentState.RUNNING) {
            setState(AgentState.FINISHED);
        }
    }






}
