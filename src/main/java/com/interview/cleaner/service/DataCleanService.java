package com.interview.cleaner.service;

import java.util.List;

/**
 * 数据清洗服务接口
 */
public interface DataCleanService<T, R> {
    
    /**
     * 清洗单条数据
     *
     * @param rawData 原始数据
     * @return 清洗后的数据
     */
    R cleanData(T rawData);
    
    /**
     * 批量清洗数据
     *
     * @param rawDataList 原始数据列表
     * @return 清洗后的数据列表
     */
    List<R> cleanBatch(List<T> rawDataList);
    
    /**
     * 清洗并保存数据
     *
     * @param rawData 原始数据
     * @return 保存后的实体ID
     */
    Long cleanAndSave(T rawData);
    
    /**
     * 批量清洗并保存数据
     *
     * @param rawDataList 原始数据列表
     * @return 保存后的实体ID列表
     */
    List<Long> cleanAndSaveBatch(List<T> rawDataList);
}


