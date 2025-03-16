package com.interview.cleaner.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 清洗后的职位数据模型
 */
@Data
public class CleanedJobData {
    /**
     * 原始ID（如果有）
     */
    private Long rawId;
    
    /**
     * 职位标题（标准化后）
     */
    private String title;
    
    /**
     * 薪资范围（标准化后）
     */
    private String salaryRange;
    
    /**
     * 最低薪资(K)
     */
    private Integer salaryMin;
    
    /**
     * 最高薪资(K)
     */
    private Integer salaryMax;
    
    /**
     * 公司名称（标准化后）
     */
    private String companyName;
    
    /**
     * 公司行业（标准化后）
     */
    private String industry;
    
    /**
     * 公司规模（标准化后）
     */
    private String companySize;
    
    /**
     * 经验要求（标准化后）
     */
    private String experienceRequired;
    
    /**
     * 学历要求（标准化后）
     */
    private String educationRequired;
    
    /**
     * 工作地点（标准化后）
     */
    private String location;
    
    /**
     * 职位描述（清洗后）
     */
    private String description;
    
    /**
     * 职位要求（清洗后）
     */
    private String requirements;
    
    /**
     * 提取的技能要求
     */
    private List<String> skills;
    
    /**
     * 提取的责任要求
     */
    private List<String> responsibilities;
    
    /**
     * 标签列表（标准化后）
     */
    private List<String> tags;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 发布日期
     */
    private LocalDate publishDate;
    
    /**
     * 数据来源
     */
    private String source;
    
    /**
     * 源URL
     */
    private String sourceUrl;
}