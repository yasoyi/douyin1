package com.alan.dy1.Service;

import java.io.IOException;

/**
 * 音频转换服务接口
 * 用于将MP3文件转换为WAV格式
 */
public interface AudioConversionService {
    
    /**
     * 将指定目录下的指定MP3文件转换为WAV格式并保存到converted_audio目录
     * 转换完成后删除原文件
     * @param sourceDir 源目录路径（包含MP3文件）
     * @param fileName 要转换的文件名
     * @throws IOException IO异常
     */
    void convertMp3ToWav(String sourceDir, String fileName) throws IOException;
}