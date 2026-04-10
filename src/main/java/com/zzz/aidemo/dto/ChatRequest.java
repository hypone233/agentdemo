package com.zzz.aidemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {


    private String chatId;


    @NotBlank(message = "消息不能为空!")
    private String userMessage;

}
