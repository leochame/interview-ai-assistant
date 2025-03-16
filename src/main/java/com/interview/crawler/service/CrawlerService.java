package com.interview.crawler.service;

import java.util.List;

/**
 * 爬虫服务接口
 */
public interface CrawlerService {
    
    /**
     * 开始爬取数据
     *
     * @param startUrl 起始URL
     * @return 任务ID
     */
    String startCrawl(String startUrl);
    
    /**
     * 开始爬取数据（使用默认起始URL）
     *
     * @return 任务ID
     */
    String startCrawl();
    
    /**
     * 停止爬虫任务
     *
     * @param taskId 任务ID
     */
    void stopCrawl(String taskId);
    
    /**
     * 获取爬虫状态
     *
     * @param taskId 任务ID
     * @return 爬虫状态
     */
    String getCrawlStatus(String taskId);
    
    /**
     * 获取所有运行中的爬虫任务
     *
     * @return 任务ID列表
     */
    List<String> getRunningTasks();
}



