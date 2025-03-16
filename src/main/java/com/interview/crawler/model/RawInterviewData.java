package com.interview.crawler.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 原始面经数据模型
 */
@Data
public class RawInterviewData {
    /**
     * 标题
     */
    private String title;
    
    /**
     * 公司名称
     */
    private String company;
    
    /**
     * 职位名称
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
     * 面经内容
     */
    private String content;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 数据来源
     */
    private String source;
    
    /**
     * 源URL
     */
    private String sourceUrl;
}

