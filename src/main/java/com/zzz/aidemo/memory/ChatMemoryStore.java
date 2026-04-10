package com.zzz.aidemo.memory;

import com.zzz.aidemo.entity.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatMemoryStore {

    private final Map<String, List<ChatMessage>> conversationMap = new ConcurrentHashMap<>();

    public List<ChatMessage> getMessage(String chatId){
        return conversationMap.computeIfAbsent(chatId,key -> new ArrayList<>());
    }

    public void addMessage(String chatId,ChatMessage chatMessage){
        List<ChatMessage> messages = conversationMap.computeIfAbsent(chatId, key -> new ArrayList<>());
        messages.add(chatMessage);
    }
    public void clear(String chatId) {
        conversationMap.remove(chatId);
    }


}
