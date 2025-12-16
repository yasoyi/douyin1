package com.alan.dy1.Service.Impl;

import com.alan.dy1.Service.AudioConversionService;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 音频转换服务实现类
 * 用于将MP3文件转换为WAV格式
 */
@Service
public class AudioConversionServiceImpl implements AudioConversionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AudioConversionServiceImpl.class);
    private static final String CONVERTED_AUDIO_FOLDER = "douyin_tools/converted_audio";
    
    // 百度语音识别要求的音频参数
    private static final int TARGET_SAMPLE_RATE = 16000; // 16kHz采样率
    private static final int TARGET_CHANNELS = 1;        // 单声道
    
    @Override
    public void convertMp3ToWav(String sourceDir, String fileName) throws IOException {
        // 创建目标目录
        Path targetPath = Paths.get(CONVERTED_AUDIO_FOLDER);
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }
        
        // 构造源文件路径
        Path sourceFilePath = Paths.get(sourceDir, fileName);
        
        // 检查源文件是否存在
        if (!Files.exists(sourceFilePath)) {
            throw new IOException("源文件不存在: " + sourceFilePath.toString());
        }
        
        // 检查文件是否为MP3格式
        if (!fileName.toLowerCase().endsWith(".mp3")) {
            throw new IOException("文件不是MP3格式: " + fileName);
        }
        
        try {
            // 转换单个文件
            convertSingleFile(sourceFilePath.toString(), CONVERTED_AUDIO_FOLDER);
            
            // 转换成功后删除原文件
            Files.delete(sourceFilePath);
            logger.info("成功删除原文件: {}", sourceFilePath.toString());
        } catch (Exception e) {
            logger.error("转换文件失败: " + fileName, e);
            throw new IOException("转换文件失败: " + fileName, e);
        }
    }
    
    private void convertSingleFile(String mp3FilePath, String targetDir) throws Exception {
        File mp3File = new File(mp3FilePath);
        String wavFileName = mp3File.getName().replaceAll("\\.mp3$", ".wav");
        String wavFilePath = targetDir + File.separator + wavFileName;
        
        convertMp3ToWavFile(mp3FilePath, wavFilePath);
        logger.info("成功转换文件: {} -> {}", mp3File.getName(), wavFileName);
    }
    
    private void convertMp3ToWavFile(String inputFile, String outputFile) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        FFmpegFrameRecorder recorder = null;
        
        try {
            // 初始化帧抓取器
            grabber.start();
            
            // 初始化帧录制器
            recorder = new FFmpegFrameRecorder(outputFile, TARGET_CHANNELS);
            recorder.setSampleRate(TARGET_SAMPLE_RATE);
            recorder.setAudioCodec(grabber.getAudioCodec());
            recorder.setAudioBitrate(grabber.getAudioBitrate());
            
            // 开始录制
            recorder.start();
            
            // 逐帧读取并写入
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                recorder.record(frame);
                if (frame != null) {
                    frame.close();
                }
            }
        } finally {
            // 释放资源
            if (grabber != null) {
                grabber.stop();
                grabber.release();
            }
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        }
    }
    
    /**
     * 获取转换后音频文件的默认保存文件夹
     * @return 转换后音频文件夹路径
     */
    public String getConvertedAudioFolder() {
        return CONVERTED_AUDIO_FOLDER;
    }
    
    /**
     * 获取目标采样率（百度语音识别要求）
     * @return 目标采样率 16000 Hz
     */
    public int getTargetSampleRate() {
        return TARGET_SAMPLE_RATE;
    }
    
    /**
     * 获取目标声道数（百度语音识别要求）
     * @return 目标声道数 1 (单声道)
     */
    public int getTargetChannels() {
        return TARGET_CHANNELS;
    }
}