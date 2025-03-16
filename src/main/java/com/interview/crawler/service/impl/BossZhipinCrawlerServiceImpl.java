
package com.interview.crawler.service.impl;

import com.interview.common.util.HttpUtil;
import com.interview.crawler.pipeline.BossZhipinPipeline;
import com.interview.crawler.processor.BossZhipinProcessor;
import com.interview.crawler.service.BossZhipinCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boss直聘爬虫服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BossZhipinCrawlerServiceImpl implements BossZhipinCrawlerService {

    private final BossZhipinProcessor bossZhipinProcessor;
    private final BossZhipinPipeline bossZhipinPipeline;
    private final Downloader httpClientDownloader;
    
    @Value("${crawler.boss.base-url}")
    private String baseUrl;
    
    @Value("${crawler.boss.keyword-url}")
    private String keywordUrl;
    
    @Value("${crawler.boss.city-url}")
    private String cityUrl;
    
    @Value("${crawler.boss.keyword-city-url}")
    private String keywordCityUrl;
    
    @Value("${crawler.thread-num:5}")
    private int threadNum;
    
    // 存储运行中的爬虫任务
    private final Map<String, Spider> runningSpiders = new ConcurrentHashMap<>();

    @Override
    public String startCrawl(String startUrl) {
        String taskId = generateTaskId();
        Spider spider = Spider.create(bossZhipinProcessor)
                .addUrl(startUrl)
                .addPipeline(bossZhipinPipeline)
                .setDownloader(httpClientDownloader)
                .thread(threadNum);
        
        runningSpiders.put(taskId, spider);
        spider.start();
        
        log.info("启动Boss直聘爬虫任务: {}, URL: {}", taskId, startUrl);
        return taskId;
    }

    @Override
    public String startCrawl() {
        return startCrawl(baseUrl);
    }

    @Override
    public void stopCrawl(String taskId) {
        Spider spider = runningSpiders.get(taskId);
        if (spider != null) {
            spider.stop();
            runningSpiders.remove(taskId);
            log.info("停止Boss直聘爬虫任务: {}", taskId);
        } else {
            log.warn("未找到指定的爬虫任务: {}", taskId);
        }
    }

    @Override
    public String getCrawlStatus(String taskId) {
        Spider spider = runningSpiders.get(taskId);
        if (spider == null) {
            return "NOT_FOUND";
        }
        return spider.getStatus().name();
    }

    @Override
    public List<String> getRunningTasks() {
        return new ArrayList<>(runningSpiders.keySet());
    }

    @Override
    public String crawlByJobKeyword(String keyword) {
        String encodedKeyword = HttpUtil.encodeUrl(keyword);
        String url = String.format(keywordUrl, encodedKeyword);
        return startCrawl(url);
    }

    @Override
    public String crawlByCity(String city) {
        String encodedCity = HttpUtil.encodeUrl(city);
        String url = String.format(cityUrl, encodedCity);
        return startCrawl(url);
    }

    @Override
    public String crawlByJobKeywordAndCity(String keyword, String city) {
        String encodedKeyword = HttpUtil.encodeUrl(keyword);
        String encodedCity = HttpUtil.encodeUrl(city);
        String url = String.format(keywordCityUrl, encodedKeyword, encodedCity);
        return startCrawl(url);
    }
    
    /**
     * 生成唯一任务ID
     */
    private String generateTaskId() {
        return "boss-" + UUID.randomUUID().toString().substring(0, 8);
    }
}