package com.zzz.aidemo.controller;

import com.zzz.aidemo.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zzz.aidemo.exception.GlobalExceptionHandler;

@WebMvcTest(ChatController.class)
@Import(GlobalExceptionHandler.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void shouldReturnBadRequestWhenUserMessageIsMissing() throws Exception {
        mockMvc.perform(post("/chat/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"chatId\": \"1\"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.content").value("参数校验失败：消息不能为空!"));

        verifyNoInteractions(chatService);
    }
}