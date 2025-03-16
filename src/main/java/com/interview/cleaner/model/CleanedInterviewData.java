package com.interview.cleaner.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 清洗后的面经数据模型
 */
@Data
public class CleanedInterviewData {
    /**
     * 原始ID（如果有）
     */
    private Long rawId;
    
    /**
     * 公司名称（标准化后）
     */
    private String company;
    
    /**
     * 职位名称（标准化后）
     */
    private String position;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 发布日期
     */
    private LocalDate publishDate;
    
    /**
     * 面经内容（清洗后）
     */
    private String content;
    
    /**
     * 提取的问题列表
     */
    private List<CleanedQuestionData> questions;
    
    /**
     * 标签列表（标准化后）
     */
    private List<String> tags;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 数据来源
     */
    private String source;
    
    /**
     * 源URL
     */
    private String sourceUrl;
}


