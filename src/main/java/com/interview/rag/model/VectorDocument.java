package com.interview.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 向量文档模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDocument {
    /**
     * 文档ID
     */
    private String id;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 向量嵌入
     */
    private List<Float> embedding;
    
    /**
     * 元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 相似度分数（用于检索结果）
     */
    private Float score;
}

