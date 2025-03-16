package com.interview.rag.service;

import com.interview.rag.model.VectorDocument;

import java.util.List;
import java.util.Map;

/**
 * 向量存储服务接口
 */
public interface VectorStoreService {
    
    /**
     * 添加文档
     *
     * @param document 向量文档
     * @return 文档ID
     */
    String addDocument(VectorDocument document);
    
    /**
     * 批量添加文档
     *
     * @param documents 向量文档列表
     * @return 文档ID列表
     */
    List<String> addDocuments(List<VectorDocument> documents);
    
    /**
     * 删除文档
     *
     * @param docId 文档ID
     * @return 操作是否成功
     */
    boolean deleteDocument(String docId);
    
    /**
     * 根据元数据删除文档
     *
     * @param metadataFilter 元数据筛选条件
     * @return 删除的文档数量
     */
    int deleteDocumentsByMetadata(Map<String, Object> metadataFilter);
    
    /**
     * 向量相似度搜索
     *
     * @param embedding 查询向量
     * @param topK 返回的最大结果数
     * @return 向量文档列表
     */
    List<VectorDocument> similaritySearch(List<Float> embedding, int topK);
    
    /**
     * 带元数据筛选的向量相似度搜索
     *
     * @param embedding 查询向量
     * @param metadataFilter 元数据筛选条件
     * @param topK 返回的最大结果数
     * @return 向量文档列表
     */
    List<VectorDocument> similaritySearchWithMetadata(List<Float> embedding, Map<String, Object> metadataFilter, int topK);
    
    /**
     * 获取集合中的文档数量
     *
     * @return 文档数量
     */
    long getDocumentCount();
    
    /**
     * 清空集合
     *
     * @return 操作是否成功
     */
    boolean clearCollection();
}

