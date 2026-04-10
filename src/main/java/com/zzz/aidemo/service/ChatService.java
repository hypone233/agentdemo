package com.zzz.aidemo.service;

import com.zzz.aidemo.dto.ChatRequest;
import org.springframework.stereotype.Service;


public interface ChatService {

    public String processChat(ChatRequest chatRequest);

}
