
package com.interview.cleaner.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 重复内容去除处理器
 */
@Slf4j
@Component
public class DuplicateRemover {

    // 存储已处理过的内容哈希值
    private final Set<String> contentHashes = Collections.synchronizedSet(new HashSet<>());
    
    /**
     * 检查内容是否重复
     *
     * @param content 要检查的内容
     * @return 如果内容重复返回true，否则返回false
     */
    public boolean isDuplicate(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        // 计算内容的MD5哈希值
        String contentHash = DigestUtils.md5Hex(content);
        
        // 检查哈希值是否已存在
        boolean isDuplicate = contentHashes.contains(contentHash);
        
        // 如果不重复，则添加到已处理集合中
        if (!isDuplicate) {
            contentHashes.add(contentHash);
        }
        
        return isDuplicate;
    }
    
    /**
     * 计算内容的相似度
     *
     * @param content1 内容1
     * @param content2 内容2
     * @return 相似度（0-1之间的值，1表示完全相同）
     */
    public double calculateSimilarity(String content1, String content2) {
        if (content1 == null || content2 == null) {
            return 0;
        }
        
        // 使用简单的Jaccard相似度计算
        Set<String> set1 = new HashSet<>(Arrays.asList(content1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(content2.split("\\s+")));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        if (union.isEmpty()) {
            return 0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * 检查内容是否与已有内容高度相似
     *
     * @param content 要检查的内容
     * @param contentList 已有内容列表
     * @param threshold 相似度阈值（0-1之间的值）
     * @return 如果存在高度相似内容返回true，否则返回false
     */
    public boolean isSimilarToExisting(String content, List<String> contentList, double threshold) {
        if (content == null || contentList == null || contentList.isEmpty()) {
            return false;
        }
        
        for (String existingContent : contentList) {
            double similarity = calculateSimilarity(content, existingContent);
            if (similarity >= threshold) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 清除集合中的重复数据
     */
    public void clearCache() {
        contentHashes.clear();
        log.info("重复内容检测缓存已清空");
    }
}
