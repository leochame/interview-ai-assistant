package com.interview.llm.service.impl;

import com.interview.llm.model.Conversation;
import com.interview.llm.model.Message;
import com.interview.llm.service.LLMService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.ChatMessage;
import dev.langchain4j.model.input.Prompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM服务实现类
 * 基于LangChain4j
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAILLMServiceImpl implements LLMService {

    private final ChatLanguageModel chatModel;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    private String modelName;
    
    @Value("${openai.temperature:0.7}")
    private double temperature;
    
    @Value("${openai.max-tokens:2048}")
    private int maxTokens;

    @Override
    public String generateText(String prompt) {
        try {
            return chatModel.generate(prompt);
        } catch (Exception e) {
            log.error("生成文本失败", e);
            return "生成文本时发生错误: " + e.getMessage();
        }
    }

    @Override
    public String generateChatResponse(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "消息列表为空";
        }
        
        try {
            List<ChatMessage> chatMessages = convertToChatMessages(messages);
            return chatModel.generate(chatMessages);
        } catch (Exception e) {
            log.error("生成聊天响应失败", e);
            return "生成聊天响应时发生错误: " + e.getMessage();
        }
    }

    @Override
    public String generateChatResponse(Conversation conversation) {
        if (conversation == null || conversation.getMessages() == null || conversation.getMessages().isEmpty()) {
            return "对话为空";
        }
        
        try {
            List<ChatMessage> chatMessages = convertToChatMessages(conversation.getMessages());
            return chatModel.generate(chatMessages);
        } catch (Exception e) {
            log.error("生成聊天响应失败", e);
            return "生成聊天响应时发生错误: " + e.getMessage();
        }
    }

    @Override
    public Map<String, String> getModelInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("model", modelName);
        info.put("temperature", String.valueOf(temperature));
        info.put("maxTokens", String.valueOf(maxTokens));
        return info;
    }
    
    /**
     * 将内部消息模型转换为LangChain4j的ChatMessage
     */
    private List<ChatMessage> convertToChatMessages(List<Message> messages) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        
        for (Message message : messages) {
            chatMessages.add(convertToChatMessage(message));
        }
        
        return chatMessages;
    }
    
    /**
     * 将单个内部消息模型转换为LangChain4j的ChatMessage
     */
    private ChatMessage convertToChatMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        
        switch (message.getRole()) {
            case SYSTEM:
                return ChatMessage.systemMessage(message.getContent());
            case USER:
                return ChatMessage.userMessage(message.getContent());
            case ASSISTANT:
                return ChatMessage.assistantMessage(message.getContent());
            default:
                throw new IllegalArgumentException("未知的消息角色: " + message.getRole());
        }
    }
}

