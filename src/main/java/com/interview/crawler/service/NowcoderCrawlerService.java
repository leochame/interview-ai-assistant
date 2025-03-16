package com.interview.crawler.service;

/**
 * 牛客网爬虫服务接口
 */
public interface NowcoderCrawlerService extends CrawlerService {
    
    /**
     * 爬取特定公司的面经
     *
     * @param company 公司名称
     * @return 任务ID
     */
    String crawlByCompany(String company);
    
    /**
     * 爬取特定职位的面经
     *
     * @param position 职位名称
     * @return 任务ID
     */
    String crawlByPosition(String position);
    
    /**
     * 爬取特定标签的面经
     *
     * @param tag 标签名称
     * @return 任务ID
     */
    String crawlByTag(String tag);
}