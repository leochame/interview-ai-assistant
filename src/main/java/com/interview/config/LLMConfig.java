package com.interview.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LLM服务配置类
 */
@Configuration
public class LLMConfig {

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${openai.embedding-model:text-embedding-ada-002}")
    private String embeddingModel;

    @Value("${openai.api-url:https://api.openai.com}")
    private String openaiApiUrl;

    @Value("${openai.timeout:60}")
    private int timeout;

    @Value("${openai.max-tokens:2048}")
    private int maxTokens;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    /**
     * 配置OpenAI Chat模型
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(openaiModel)
                .baseUrl(openaiApiUrl)
                .timeout(Duration.ofSeconds(timeout))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }

    /**
     * 配置OpenAI Embedding模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openaiApiKey)
                .modelName(embeddingModel)
                .baseUrl(openaiApiUrl)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }
}