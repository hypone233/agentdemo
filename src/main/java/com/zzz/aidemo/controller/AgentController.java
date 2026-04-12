package com.zzz.aidemo.controller;


import com.zzz.aidemo.agent.DemoAgent;
import com.zzz.aidemo.dto.ChatRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequestMapping("/agent")
@RestController
public class AgentController {

    @PostMapping("/run")
    public String runDemoAgentSync(@Valid @RequestBody ChatRequest chatRequest){
        DemoAgent agent = new DemoAgent(chatRequest.getUserMessage());

        return agent.run();
    }

    @PostMapping("stream")
    public SseEmitter runDemoAgentSse(@Valid @RequestBody ChatRequest chatRequest) {
        DemoAgent agent = new DemoAgent(chatRequest.getUserMessage());
        return agent.runStream();
    }



}
