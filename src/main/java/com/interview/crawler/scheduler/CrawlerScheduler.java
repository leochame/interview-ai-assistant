package com.interview.crawler.scheduler;

import com.interview.crawler.service.BossZhipinCrawlerService;
import com.interview.crawler.service.NowcoderCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 爬虫调度器
 * 定时启动爬虫任务
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CrawlerScheduler {

    private final BossZhipinCrawlerService bossZhipinCrawlerService;
    private final NowcoderCrawlerService nowcoderCrawlerService;
    
    @Value("${crawler.scheduler.enabled:false}")
    private boolean schedulerEnabled;
    
    @Value("${crawler.scheduler.job-keywords:Java,Python,Go,前端,后端,全栈,数据分析,算法,人工智能}")
    private String jobKeywords;
    
    @Value("${crawler.scheduler.cities:北京,上海,广州,深圳,杭州,成都}")
    private String cities;
    
    @Value("${crawler.scheduler.companies:阿里,腾讯,字节,百度,美团,京东,滴滴,华为}")
    private String companies;
    
    /**
     * Boss直聘职位爬虫 - 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleBossZhipinCrawler() {
        if (!schedulerEnabled) {
            log.info("爬虫调度已禁用，跳过Boss直聘职位爬取");
            return;
        }
        
        log.info("开始调度Boss直聘职位爬虫任务");
        
        try {
            List<String> keywordList = Arrays.asList(jobKeywords.split(","));
            List<String> cityList = Arrays.asList(cities.split(","));
            
            for (String keyword : keywordList) {
                for (String city : cityList) {
                    String taskId = bossZhipinCrawlerService.crawlByJobKeywordAndCity(keyword.trim(), city.trim());
                    log.info("启动Boss直聘爬虫任务: 关键词[{}], 城市[{}], 任务ID[{}]", keyword, city, taskId);
                    
                    // 间隔一段时间再启动下一个任务，避免频繁请求
                    Thread.sleep(30000);
                }
            }
        } catch (Exception e) {
            log.error("调度Boss直聘爬虫任务出错", e);
        }
    }
    
    /**
     * 牛客网面经爬虫 - 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduleNowcoderCrawler() {
        if (!schedulerEnabled) {
            log.info("爬虫调度已禁用，跳过牛客网面经爬取");
            return;
        }
        
        log.info("开始调度牛客网面经爬虫任务");
        
        try {
            // 按公司爬取
            List<String> companyList = Arrays.asList(companies.split(","));
            for (String company : companyList) {
                String taskId = nowcoderCrawlerService.crawlByCompany(company.trim());
                log.info("启动牛客网爬虫任务: 公司[{}], 任务ID[{}]", company, taskId);
                
                // 间隔一段时间再启动下一个任务，避免频繁请求
                Thread.sleep(20000);
            }
            
            // 按职位爬取
            List<String> positionList = Arrays.asList(jobKeywords.split(","));
            for (String position : positionList) {
                String taskId = nowcoderCrawlerService.crawlByPosition(position.trim());
                log.info("启动牛客网爬虫任务: 职位[{}], 任务ID[{}]", position, taskId);
                
                // 间隔一段时间再启动下一个任务，避免频繁请求
                Thread.sleep(20000);
            }
        } catch (Exception e) {
            log.error("调度牛客网爬虫任务出错", e);
        }
    }
}