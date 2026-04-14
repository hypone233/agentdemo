package com.zzz.aidemo.controller;



import com.zzz.aidemo.agent.ZManus;
import com.zzz.aidemo.dto.ChatRequest;
import com.zzz.aidemo.service.ManusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/agent")
public class ManusController {

    @Autowired
    private ManusService manusService;

    @GetMapping("/sync")
    public String runSync(@RequestBody @Valid ChatRequest chatRequest) {
        return manusService.run(chatRequest.getUserMessage(),chatRequest.getChatId());
    }

    @GetMapping("/stream")
    public SseEmitter runStream(@RequestBody @Valid ChatRequest chatRequest) {
        return manusService.runStream(chatRequest.getUserMessage(),chatRequest.getChatId());
    }
}
