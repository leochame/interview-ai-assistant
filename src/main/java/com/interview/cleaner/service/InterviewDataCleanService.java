package com.interview.cleaner.service;

import com.interview.cleaner.model.CleanedInterviewData;
import com.interview.crawler.model.RawInterviewData;

import java.util.List;

/**
 * 面经数据清洗服务接口
 */
public interface InterviewDataCleanService extends DataCleanService<RawInterviewData, CleanedInterviewData> {
    
    /**
     * 从面经内容中提取问题答案对
     *
     * @param content 面经内容
     * @return 清洗后的面经数据（含问题列表）
     */
    CleanedInterviewData extractQuestionsFromContent(String content);
    
    /**
     * 对面经内容进行标准化处理
     *
     * @param content 原始内容
     * @return 标准化后的内容
     */
    String normalizeContent(String content);
    
    /**
     * 识别面经内容的主题和技术领域
     *
     * @param content 面经内容
     * @return 标签列表
     */
    List<String> extractTags(String content);
    
    /**
     * 清洗特定公司的所有面经数据
     *
     * @param companyName 公司名称
     * @return 处理的记录数
     */
    int cleanCompanyInterviews(String companyName);
}
