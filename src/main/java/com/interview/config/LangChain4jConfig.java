package com.interview.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.jdbc.JdbcEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * LangChain4j RAG 配置类
 */
@Configuration
@RequiredArgsConstructor
public class LangChain4jConfig {

    private final DataSource dataSource;
    
    @Value("${rag.collection.interview}")
    private String interviewCollection;
    
    @Value("${rag.collection.job}")
    private String jobCollection;
    
    @Value("${rag.embedding.dimensions:1536}")
    private int embeddingDimensions;
    
    @Value("${rag.embedding.model:text-embedding-ada-002}")
    private String embeddingModel;
    
    @Value("${rag.embedding.api-key}")
    private String embeddingApiKey;
    
    @Value("${rag.embedding.api-url:https://api.openai.com}")
    private String embeddingApiUrl;

    /**
     * 配置 OpenAI 嵌入模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingApiKey)
                .modelName(embeddingModel)
                .baseUrl(embeddingApiUrl)
                .dimensions(embeddingDimensions)
                .build();
    }
    
    /**
     * 配置面试知识库嵌入存储
     */
    @Bean(name = "interviewEmbeddingStore")
    public EmbeddingStore<Embedding> interviewEmbeddingStore() {
        return JdbcEmbeddingStore.builder()
                .dataSource(dataSource)
                .tableName("interview_vector_store")
                .contentField("content")
                .metadataField("metadata")
                .embeddingField("embedding")
                .build();
    }
    
    /**
     * 配置职位知识库嵌入存储
     */
    @Bean(name = "jobEmbeddingStore")
    public EmbeddingStore<Embedding> jobEmbeddingStore() {
        return JdbcEmbeddingStore.builder()
                .dataSource(dataSource)
                .tableName("job_vector_store")
                .contentField("content")
                .metadataField("metadata")
                .embeddingField("embedding")
                .build();
    }
}