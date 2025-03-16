package com.interview.cleaner.processor;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本标准化处理器
 * 负责对文本内容进行清洗和标准化
 */
@Component
public class TextNormalizer {

    // HTML标签允许列表
    private static final Safelist HTML_WHITELIST = Safelist.relaxed()
            .addTags("div", "span", "pre", "code", "h1", "h2", "h3", "h4", "h5", "h6")
            .addAttributes("div", "class")
            .addAttributes("span", "class")
            .addAttributes("code", "class");

    // 常见的脏数据正则表达式
    private static final List<Pattern> NOISE_PATTERNS = Arrays.asList(
            Pattern.compile("来源：.*?牛客网", Pattern.CASE_INSENSITIVE),
            Pattern.compile("https?://[^\\s]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\[\\d+赞\\]"),
            Pattern.compile("编辑于.*?$"),
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}\\s+"),
            Pattern.compile("赞同\\s*\\d+"),
            Pattern.compile("评论\\s*\\d+"),
            Pattern.compile("收藏\\s*\\d+"),
            Pattern.compile("转发\\s*\\d+")
    );
    
    /**
     * 清洗HTML内容
     *
     * @param html 原始HTML
     * @return 清洗后的HTML
     */
    public String cleanHtml(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        
        // 解析HTML
        Document doc = Jsoup.parse(html);
        
        // 移除无关元素
        doc.select("script, style, iframe, img, noscript").remove();
        
        // 使用白名单清洗HTML
        String cleanHtml = Jsoup.clean(doc.body().html(), HTML_WHITELIST);
        
        return cleanHtml;
    }
    
    /**
     * 清洗文本内容
     * 
     * @param text 原始文本
     * @return 清洗后的文本
     */
    public String cleanText(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        
        // 移除HTML标签
        String plainText = Jsoup.parse(text).text();
        
        // 移除干扰内容
        for (Pattern pattern : NOISE_PATTERNS) {
            plainText = pattern.matcher(plainText).replaceAll("");
        }
        
        // 标准化空白字符
        plainText = plainText.replaceAll("\\s+", " ").trim();
        
        return plainText;
    }
    
    /**
     * 标准化公司名称
     *
     * @param companyName 原始公司名称
     * @return 标准化后的公司名称
     */
    public String normalizeCompanyName(String companyName) {
        if (StringUtils.isBlank(companyName)) {
            return "";
        }
        
        // 移除括号及其内容
        String normalized = companyName.replaceAll("\\(.*?\\)|（.*?）", "");
        
        // 移除常见后缀
        normalized = normalized.replaceAll("股份有限公司|有限公司|科技|集团|公司", "");
        
        // 移除前后空白
        normalized = normalized.trim();
        
        return normalized;
    }
    
    /**
     * 标准化职位名称
     *
     * @param position 原始职位名称
     * @return 标准化后的职位名称
     */
    public String normalizePosition(String position) {
        if (StringUtils.isBlank(position)) {
            return "";
        }
        
        // 移除括号及其内容
        String normalized = position.replaceAll("\\(.*?\\)|（.*?）", "");
        
        // 标准化职位级别
        normalized = normalized.replaceAll("(?i)初级|junior", "初级");
        normalized = normalized.replaceAll("(?i)中级|middle", "中级");
        normalized = normalized.replaceAll("(?i)高级|senior", "高级");
        normalized = normalized.replaceAll("(?i)专家|expert", "专家");
        normalized = normalized.replaceAll("(?i)资深", "高级");
        
        // 移除前后空白
        normalized = normalized.trim();
        
        return normalized;
    }
    
    /**
     * 提取问题文本
     *
     * @param text 包含问题的文本
     * @return 提取的问题文本
     */
    public String extractQuestion(String text) {
        // 问题通常以问号结尾
        Pattern questionPattern = Pattern.compile("(.*?\\?|.*?？|.*?如何|.*?为什么|.*?怎么|.*?是什么)");
        Matcher matcher = questionPattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return text;
    }
}
