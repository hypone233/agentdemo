package com.zzz.aidemo.agent;

import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
public abstract class BaseAgent {

    private final String userMessage;

    private final int maxSteps;

    private final List<String> stepRecords = new ArrayList<>();

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;

    protected BaseAgent(String userMessage, int maxSteps) {
        this.userMessage = userMessage;
        this.maxSteps = maxSteps;
    }


    public String run(){
        ensureRunnable();

        state = AgentState.RUNNING;

        try {
            while(state == AgentState.RUNNING && currentStep < maxSteps){

                currentStep++;

                String stepResult = step();

                String record = buildStepRecord(currentStep, stepResult);
                stepRecords.add(record);

                if(shouldFinish(stepResult)){
                    state = AgentState.FINISHED;
                }

            }
            if(state == AgentState.RUNNING){
                state = AgentState.FINISHED;
                stepRecords.add("达到最大步数，强制结束");
            }
            return String.join("/n",stepRecords);

        }catch (Exception e){
            state = AgentState.ERROR;
            throw new RuntimeException("Agent 执行失败", e);
        }

    }

    public SseEmitter runStream(){

        ensureRunnable();

        SseEmitter sseEmitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            state = AgentState.RUNNING;

            try{
                sseEmitter.send(SseEmitter.event()
                        .name("start")
                        .data("Agent 开始执行"));


                while (state == AgentState.RUNNING && currentStep < maxSteps) {
                    currentStep++;

                    sseEmitter.send(SseEmitter.event()
                            .name("step-start")
                            .data("开始第 " + currentStep + " 步"));

                    String stepResult = step();
                    String record = buildStepRecord(currentStep, stepResult);
                    stepRecords.add(record);

                    sseEmitter.send(SseEmitter.event()
                            .name("step")
                            .data(record));

                    if (shouldFinish(stepResult)) {
                        state = AgentState.FINISHED;
                    }
                }
                if (state == AgentState.RUNNING) {
                    state = AgentState.FINISHED;
                    sseEmitter.send(SseEmitter.event()
                            .name("finish")
                            .data("达到最大步数，强制结束"));
                } else {
                    sseEmitter.send(SseEmitter.event()
                            .name("finish")
                            .data("Agent 执行完成"));
                }
                sseEmitter.complete();

            } catch (Exception e) {
                state = AgentState.ERROR;
                sendErrorSafely(sseEmitter, e);
                sseEmitter.completeWithError(e);
            }

        });
        return sseEmitter;

    }

    protected abstract String step();

    private void ensureRunnable() {
        if (state != AgentState.IDLE) {
            throw new IllegalStateException("当前 Agent 只能执行一次");
        }
    }

    private String buildStepRecord(int stepNumber, String stepResult) {
        return "Step " + stepNumber + "： " + stepResult;
    }

    protected boolean shouldFinish(String stepResult) {
        return false;
    }

    private void sendErrorSafely(SseEmitter emitter, Exception exception) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("Agent 执行异常： " + exception.getMessage()));
        } catch (IOException ignored) {
            // 这里忽略发送 error 事件时的异常，避免二次异常污染主流程
        }
    }






}
