package com.interview.llm.service;

import com.interview.llm.model.Conversation;
import com.interview.llm.model.Message;

import java.util.List;
import java.util.Map;

/**
 * LLM服务接口
 */
public interface LLMService {
    
    /**
     * 生成文本响应
     *
     * @param prompt 提示词
     * @return 生成的响应
     */
    String generateText(String prompt);
    
    /**
     * 生成聊天响应
     *
     * @param messages 消息历史
     * @return 生成的响应
     */
    String generateChatResponse(List<Message> messages);
    
    /**
     * 生成聊天响应
     *
     * @param conversation 对话对象
     * @return 生成的响应
     */
    String generateChatResponse(Conversation conversation);
    
    /**
     * 获取模型信息
     *
     * @return 模型信息映射
     */
    Map<String, String> getModelInfo();
}


