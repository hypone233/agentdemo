package com.zzz.aidemo.controller;



import com.zzz.aidemo.agent.ZManus;
import com.zzz.aidemo.dto.ChatRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/agent")
public class ManusController {

    private final ZManus zManus;

    public ManusController(ZManus zManus) {
        this.zManus = zManus;
    }

    @GetMapping("/sync")
    public String runSync(@RequestBody @Valid ChatRequest chatRequest) {
        return zManus.run(chatRequest.getUserMessage());
    }

    @GetMapping("/stream")
    public SseEmitter runStream(@RequestBody @Valid ChatRequest chatRequest) {
        return zManus.runStream(chatRequest.getUserMessage());
    }
}
