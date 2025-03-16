package com.interview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * WebMagic爬虫框架配置类
 */
@Configuration
public class WebMagicConfig {

    @Value("${crawler.thread-num:5}")
    private int threadNum;

    @Value("${crawler.retry-times:3}")
    private int retryTimes;

    @Value("${crawler.sleep-time:1000}")
    private int sleepTime;

    @Value("${crawler.timeout:10000}")
    private int timeout;

    @Value("${crawler.user-agent-list:}")
    private List<String> userAgentList;

    @Value("${crawler.use-proxy:false}")
    private boolean useProxy;

    @Value("${crawler.proxy-list:}")
    private List<String> proxyList;

    /**
     * 获取随机UserAgent
     */
    @Bean
    public String randomUserAgent() {
        if (userAgentList == null || userAgentList.isEmpty()) {
            // 默认UserAgent列表
            userAgentList = new ArrayList<>();
            userAgentList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            userAgentList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15");
            userAgentList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0");
            userAgentList.add("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
            userAgentList.add("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1");
        }
        return userAgentList.get(new Random().nextInt(userAgentList.size()));
    }

    /**
     * 配置下载器
     */
    @Bean
    public Downloader httpClientDownloader() {
        HttpClientDownloader downloader = new HttpClientDownloader();
        
        // 设置代理
        if (useProxy && proxyList != null && !proxyList.isEmpty()) {
            List<Proxy> proxies = new ArrayList<>();
            for (String proxyStr : proxyList) {
                String[] parts = proxyStr.split(":");
                if (parts.length == 2) {
                    proxies.add(new Proxy(parts[0], Integer.parseInt(parts[1])));
                }
            }
            downloader.setProxyProvider(SimpleProxyProvider.from(proxies.toArray(new Proxy[0])));
        }
        
        return downloader;
    }
}