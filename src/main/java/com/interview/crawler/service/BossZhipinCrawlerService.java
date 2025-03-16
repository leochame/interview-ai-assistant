
package com.interview.crawler.service;

/**
 * Boss直聘爬虫服务接口
 */
public interface BossZhipinCrawlerService extends CrawlerService {
    
    /**
     * 按职位关键词爬取
     *
     * @param keyword 职位关键词
     * @return 任务ID
     */
    String crawlByJobKeyword(String keyword);
    
    /**
     * 按地区爬取
     *
     * @param city 城市
     * @return 任务ID
     */
    String crawlByCity(String city);
    
    /**
     * 按职位关键词和地区爬取
     *
     * @param keyword 职位关键词
     * @param city 城市
     * @return 任务ID
     */
    String crawlByJobKeywordAndCity(String keyword, String city);
}
