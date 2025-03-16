package com.interview.crawler.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 原始职位数据模型
 */
@Data
public class RawJobData {
    /**
     * 职位标题
     */
    private String title;
    
    /**
     * 薪资范围
     */
    private String salaryRange;
    
    /**
     * 最低薪资(K)
     */
    private String salaryMin;
    
    /**
     * 最高薪资(K)
     */
    private String salaryMax;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 公司行业
     */
    private String industry;
    
    /**
     * 公司规模
     */
    private String companySize;
    
    /**
     * 经验要求
     */
    private String experienceRequired;
    
    /**
     * 学历要求
     */
    private String educationRequired;
    
    /**
     * 工作地点
     */
    private String location;
    
    /**
     * 职位描述
     */
    private String description;
    
    /**
     * 公司描述
     */
    private String companyDescription;
    
    /**
     * 业务方向/产品
     */
    private String businessDirection;
    
    /**
     * 技能标签
     */
    private List<String> tags;
    
    /**
     * 招聘者姓名
     */
    private String recruiterName;
    
    /**
     * 招聘者职位
     */
    private String recruiterTitle;
    
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