package com.interview.llm.service;

import com.interview.llm.model.Conversation;
import com.interview.llm.model.InterviewFeedback;

import java.util.List;
import java.util.Map;

/**
 * 面试模拟服务接口
 */
public interface InterviewSimulatorService {
    
    /**
     * 创建新的面试模拟会话
     *
     * @param userId 用户ID
     * @param jobDescriptionId 职位ID
     * @param simulationType 模拟类型
     * @return 会话ID
     */
    Long createSimulation(Long userId, Long jobDescriptionId, String simulationType);
    
    /**
     * 开始面试模拟
     *
     * @param simulationId 模拟ID
     * @return 面试官开场白
     */
    String startSimulation(Long simulationId);
    
    /**
     * 发送用户消息并获取回复
     *
     * @param simulationId 模拟ID
     * @param userMessage 用户消息
     * @return 面试官回复
     */
    String sendMessage(Long simulationId, String userMessage);
    
    /**
     * 结束面试模拟并生成反馈
     *
     * @param simulationId 模拟ID
     * @return 面试反馈
     */
    InterviewFeedback endSimulation(Long simulationId);
    
    /**
     * 获取面试模拟对话历史
     *
     * @param simulationId 模拟ID
     * @return 对话历史
     */
    Conversation getSimulationHistory(Long simulationId);
    
    /**
     * 获取面试问题列表
     *
     * @param jobDescriptionId 职位ID
     * @param count 问题数量
     * @return 面试问题列表
     */
    List<String> generateInterviewQuestions(Long jobDescriptionId, int count);
    
    /**
     * 评估面试答案
     *
     * @param question 面试问题
     * @param answer 用户回答
     * @return 评估结果
     */
    Map<String, Object> evaluateAnswer(String question, String answer);
    
    /**
     * 提供面试指导建议
     *
     * @param userId 用户ID
     * @return 指导建议
     */
    String provideGuidance(Long userId);
}