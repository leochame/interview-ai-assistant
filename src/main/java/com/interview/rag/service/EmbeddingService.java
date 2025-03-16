package com.interview.rag.service;

import java.util.List;

/**
 * 嵌入服务接口
 */
public interface EmbeddingService {
    
    /**
     * 生成文本嵌入向量
     *
     * @param text 输入文本
     * @return 嵌入向量
     */
    List<Float> createEmbedding(String text);
    
    /**
     * 批量生成文本嵌入向量
     *
     * @param texts 输入文本列表
     * @return 嵌入向量列表
     */
    List<List<Float>> createEmbeddings(List<String> texts);
    
    /**
     * 计算两个向量之间的相似度
     *
     * @param embedding1 向量1
     * @param embedding2 向量2
     * @return 相似度分数(0-1之间)
     */
    float calculateSimilarity(List<Float> embedding1, List<Float> embedding2);
}