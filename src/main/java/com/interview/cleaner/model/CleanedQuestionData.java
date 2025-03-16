package com.interview.cleaner.model;

import lombok.Data;

/**
 * 清洗后的问题数据模型
 */
@Data
public class CleanedQuestionData {
    /**
     * 问题内容
     */
    private String question;
    
    /**
     * 回答内容
     */
    private String answer;
    
    /**
     * 问题分类
     */
    private String category;
    
    /**
     * 难度估计（1-5）
     */
    private Integer difficulty;
}
