package com.alan.dy1.Service;

/**
 * 全流程服务接口
 * 用于启动所有子服务
 */
public interface AllStart {
    /**
     * 启动所有子服务，按顺序执行
     */
    void startAllServices();
}