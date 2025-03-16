package com.interview.rag.util;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 相似度计算工具
 * 提供向量相似度计算的实用方法
 */
@Component
public class SimilarityCalculator {

    /**
     * 计算余弦相似度
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度得分(0-1之间)
     */
    public float cosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0f;
        }
        
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度不匹配: " + vector1.size() + " vs " + vector2.size());
        }
        
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }
        
        if (norm1 <= 0 || norm2 <= 0) {
            return 0f;
        }
        
        return dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 计算欧氏距离
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 欧氏距离
     */
    public float euclideanDistance(List<Float> vector1, List<Float> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return Float.MAX_VALUE;
        }
        
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度不匹配: " + vector1.size() + " vs " + vector2.size());
        }
        
        float sumSquared = 0.0f;
        
        for (int i = 0; i < vector1.size(); i++) {
            float diff = vector1.get(i) - vector2.get(i);
            sumSquared += diff * diff;
        }
        
        return (float) Math.sqrt(sumSquared);
    }
    
    /**
     * 计算欧氏距离并转换为相似度
     * 将距离转换为0-1之间的相似度分数，距离越小，相似度越高
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度得分(0-1之间)
     */
    public float euclideanSimilarity(List<Float> vector1, List<Float> vector2) {
        float distance = euclideanDistance(vector1, vector2);
        
        if (distance == Float.MAX_VALUE) {
            return 0f;
        }
        
        // 使用高斯核函数将距离转换为相似度
        // 距离为0时相似度为1，距离越大相似度越接近0
        return (float) Math.exp(-distance);
    }
    
    /**
     * 计算点积
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 点积值
     */
    public float dotProduct(List<Float> vector1, List<Float> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0f;
        }
        
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度不匹配: " + vector1.size() + " vs " + vector2.size());
        }
        
        float dotProduct = 0.0f;
        
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
        }
        
        return dotProduct;
    }
    
    /**
     * 归一化向量
     *
     * @param vector 输入向量
     * @return 归一化后的向量
     */
    public List<Float> normalize(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            return vector;
        }
        
        float norm = 0.0f;
        for (float value : vector) {
            norm += value * value;
        }
        
        if (norm <= 0) {
            return vector;
        }
        
        float multiplier = 1.0f / (float) Math.sqrt(norm);
        
        for (int i = 0; i < vector.size(); i++) {
            vector.set(i, vector.get(i) * multiplier);
        }
        
        return vector;
    }
}