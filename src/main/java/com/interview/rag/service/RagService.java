package com.interview.rag.service;

import com.interview.rag.model.QueryResult;
import com.interview.rag.model.RetrievalContext;
import com.interview.rag.model.VectorDocument;

import java.util.List;
import java.util.Map;

/**
 * RAG服务接口
 */
public interface RagService {
    
    /**
     * 添加文档到向量库
     *
     * @param content 文档内容
     * @param metadata 元数据
     * @return 文档ID
     */
    String addDocument(String content, Map<String, Object> metadata);
    
    /**
     * 批量添加文档到向量库
     *
     * @param contents 文档内容列表
     * @param metadataList 元数据列表
     * @return 文档ID列表
     */
    List<String> addDocuments(List<String> contents, List<Map<String, Object>> metadataList);
    
    /**
     * 从向量库中删除文档
     *
     * @param docId 文档ID
     * @return 操作是否成功
     */
    boolean deleteDocument(String docId);
    
    /**
     * 按查询检索相关文档
     *
     * @param query 查询文本
     * @param topK 返回的最大结果数
     * @return 检索结果
     */
    QueryResult search(String query, int topK);
    
    /**
     * 按查询检索相关文档，并构建检索上下文
     *
     * @param query 查询文本
     * @param topK 返回的最大结果数
     * @return 检索上下文
     */
    RetrievalContext searchWithContext(String query, int topK);
    
    /**
     * 按元数据筛选条件进行查询
     *
     * @param query 查询文本
     * @param metadataFilter 元数据筛选条件
     * @param topK 返回的最大结果数
     * @return 检索结果
     */
    QueryResult searchWithMetadata(String query, Map<String, Object> metadataFilter, int topK);
    
    /**
     * 导入现有数据到向量库
     *
     * @param sourceType 源数据类型（如"interview"或"job"）
     * @param batchSize 每批处理的数据量
     * @return 导入的文档数量
     */
    int importExistingData(String sourceType, int batchSize);
}
