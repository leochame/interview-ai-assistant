package com.interview.crawler.service.impl;

import com.interview.common.util.HttpUtil;
import com.interview.crawler.pipeline.NowcoderPipeline;
import com.interview.crawler.processor.NowcoderProcessor;
import com.interview.crawler.service.NowcoderCrawlerService;
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
import java.util.stream.Collectors;

/**
 * 牛客网爬虫服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NowcoderCrawlerServiceImpl implements NowcoderCrawlerService {

    private final NowcoderProcessor nowcoderProcessor;
    private final NowcoderPipeline nowcoderPipeline;
    private final Downloader httpClientDownloader;
    
    @Value("${crawler.nowcoder.base-url}")
    private String baseUrl;
    
    @Value("${crawler.nowcoder.company-url}")
    private String companyUrl;
    
    @Value("${crawler.nowcoder.position-url}")
    private String positionUrl;
    
    @Value("${crawler.nowcoder.tag-url}")
    private String tagUrl;
    
    @Value("${crawler.thread-num:5}")
    private int threadNum;
    
    // 存储运行中的爬虫任务
    private final Map<String, Spider> runningSpiders = new ConcurrentHashMap<>();

    @Override
    public String startCrawl(String startUrl) {
        String taskId = generateTaskId();
        Spider spider = Spider.create(nowcoderProcessor)
                .addUrl(startUrl)
                .addPipeline(nowcoderPipeline)
                .setDownloader(httpClientDownloader)
                .thread(threadNum);
        
        runningSpiders.put(taskId, spider);
        spider.start();
        
        log.info("启动牛客网爬虫任务: {}, URL: {}", taskId, startUrl);
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
            log.info("停止牛客网爬虫任务: {}", taskId);
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
    public String crawlByCompany(String company) {
        String encodedCompany = HttpUtil.encodeUrl(company);
        String url = String.format(companyUrl, encodedCompany);
        return startCrawl(url);
    }

    @Override
    public String crawlByPosition(String position) {
        String encodedPosition = HttpUtil.encodeUrl(position);
        String url = String.format(positionUrl, encodedPosition);
        return startCrawl(url);
    }

    @Override
    public String crawlByTag(String tag) {
        String encodedTag = HttpUtil.encodeUrl(tag);
        String url = String.format(tagUrl, encodedTag);
        return startCrawl(url);
    }
    
    /**
     * 生成唯一任务ID
     */
    private String generateTaskId() {
        return "nowcoder-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
