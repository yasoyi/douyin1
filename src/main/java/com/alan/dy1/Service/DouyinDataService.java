package com.alan.dy1.Service;

import com.alan.dy1.domain.Douyin;

import java.util.List;

public interface DouyinDataService {
    /**
     * 保存抖音数据
     * @param douyin 抖音数据对象
     * @return 保存结果消息
     */
    String saveDouyinData(Douyin douyin);
    
    /**
     * 获取所有抖音数据
     * @return 抖音数据列表
     */
    List<Douyin> getAllDouyinData();
}