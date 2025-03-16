
package com.interview.llm.service;

import com.interview.llm.prompt.PromptTemplate;

import java.util.Map;

/**
 * 提示词服务接口
 */
public interface PromptService {
    
    /**
     * 加载提示词模板
     *
     * @param templateName 模板名称
     * @return 提示词模板
     */
    PromptTemplate loadTemplate(String templateName);
    
    /**
     * 格式化提示词
     *
     * @param template 提示词模板
     * @param variables 变量映射
     * @return 格式化后的提示词
     */
    String formatPrompt(PromptTemplate template, Map<String, Object> variables);
    
    /**
     * 格式化提示词
     *
     * @param templateName 模板名称
     * @param variables 变量映射
     * @return 格式化后的提示词
     */
    String formatPrompt(String templateName, Map<String, Object> variables);
    
    /**
     * 保存提示词模板
     *
     * @param template 提示词模板
     */
    void saveTemplate(PromptTemplate template);
}