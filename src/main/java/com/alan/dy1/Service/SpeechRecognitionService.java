package com.alan.dy1.Service;

/**
 * 语音识别服务接口
 * 用于将音频文件转换为文本
 */
public interface SpeechRecognitionService {
    
    /**
     * 将指定路径的音频文件转换为文本
     * @param audioFilePath 音频文件路径
     * @return 识别出的文本
     * @throws Exception 识别过程中可能发生的异常
     */
    String recognizeSpeech(String audioFilePath) throws Exception;
}