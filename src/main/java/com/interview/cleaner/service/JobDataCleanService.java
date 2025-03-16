package com.interview.cleaner.service;

import com.interview.cleaner.model.CleanedJobData;
import com.interview.crawler.model.RawJobData;

import java.util.List;

/**
 * 职位数据清洗服务接口
 */
public interface JobDataCleanService extends DataCleanService<RawJobData, CleanedJobData> {
    
    /**
     * 从职位描述中提取技能要求
     *
     * @param description 职位描述
     * @return 技能列表
     */
    List<String> extractSkills(String description);
    
    /**
     * 从职位描述中提取工作职责
     *
     * @param description 职位描述
     * @return 工作职责列表
     */
    List<String> extractResponsibilities(String description);
    
    /**
     * 标准化薪资范围
     *
     * @param salaryRange 原始薪资范围文本
     * @return 标准化后的薪资对象，包含最小值和最大值
     */
    CleanedJobData.SalaryRange normalizeSalaryRange(String salaryRange);
    
    /**
     * 标准化工作地点
     *
     * @param location 原始工作地点文本
     * @return 标准化后的工作地点
     */
    String normalizeLocation(String location);
    
    /**
     * 标准化职位标题
     *
     * @param title 原始职位标题
     * @return 标准化后的职位标题
     */
    String normalizeJobTitle(String title);
    
    /**
     * 清洗特定公司的所有职位数据
     *
     * @param companyName 公司名称
     * @return 处理的记录数
     */
    int cleanCompanyJobs(String companyName);
}