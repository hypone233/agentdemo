package com.zzz.aidemo.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import reactor.core.publisher.Flux;
import java.util.function.Function;

public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {


    public static final Function<ChatClientRequest, String> DEFAULT_REQUEST_TO_STRING = ChatClientRequest::toString;
    public static final Function<ChatResponse, String> DEFAULT_RESPONSE_TO_STRING = ModelOptionsUtils::toJsonStringPrettyPrinter;

    private static final Logger logger = LoggerFactory.getLogger(MyLoggerAdvisor.class);
    private final Function<ChatClientRequest, String> requestToString;
    private final Function<ChatResponse, String> responseToString;
    private final int order;

    // 构造方法 1
    public MyLoggerAdvisor() {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, 0);
    }

    // 构造方法 2
    public MyLoggerAdvisor(int order) {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, order);
    }

    // 构造方法 3（核心）
    public MyLoggerAdvisor(Function<ChatClientRequest, String> requestToString,
                           Function<ChatResponse, String> responseToString,
                           int order) {
        this.requestToString = requestToString != null ? requestToString : DEFAULT_REQUEST_TO_STRING;
        this.responseToString = responseToString != null ? responseToString : DEFAULT_RESPONSE_TO_STRING;
        this.order = order;
    }

    // ====================== 方法重写 ======================
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    protected void logRequest(ChatClientRequest request) {
        logger.info("【AI 请求】: {}", this.requestToString.apply(request));
    }

    protected void logResponse(ChatClientResponse chatClientResponse) {
        logger.info("【AI 响应】: {}", this.responseToString.apply(chatClientResponse.chatResponse()));
    }

    // ====================== 接口方法 ======================
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return MyLoggerAdvisor.class.getSimpleName();
    }

    // ====================== Builder 静态内部类 ======================
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Function<ChatClientRequest, String> requestToString;
        private Function<ChatResponse, String> responseToString;
        private int order = 0;

        private Builder() {}

        public Builder requestToString(Function<ChatClientRequest, String> requestToString) {
            this.requestToString = requestToString;
            return this;
        }

        public Builder responseToString(Function<ChatResponse, String> responseToString) {
            this.responseToString = responseToString;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public MyLoggerAdvisor build() {
            return new MyLoggerAdvisor(this.requestToString, this.responseToString, this.order);
        }
    }
}