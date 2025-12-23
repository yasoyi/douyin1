package com.alan.dy1.Service.Impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.alan.dy1.Service.SpeechRecognitionService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;

/**
 * 语音识别服务实现类（修正鉴权逻辑）
 * 使用百度语音识别API将音频文件转换为文本
 */
@Service
public class SpeechRecognitionServiceImpl implements SpeechRecognitionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpeechRecognitionServiceImpl.class);
    
    // 百度API配置
    @Value("${baidu.api-key:your_baidu_api_key}")
    private String apiKey;
    
    @Value("${baidu.secret-key:your_baidu_secret_key}")
    private String secretKey;

    @Value("${baidu.mac-address:your_mac-address}")
    private String macAddress;
    
    // 鉴权接口URL（获取Access Token）
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";
    // 语音识别接口URL
    private static final String ASR_URL = "https://vop.baidu.com/server_api";

    // 缓存Access Token（有效期30天，避免重复请求）
    private String accessToken;
    // Token过期时间（毫秒）
    private long tokenExpireTime;
    
    @Override
    public String recognizeSpeech(String audioFilePath) throws Exception {
        // 1. 读取本地音频文件（WAV格式，16k采样率、单声道、16bit位深）
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            throw new RuntimeException("音频文件不存在：" + audioFilePath);
        }
        
        // 2. 音频文件转 Base64 编码
        String base64Audio = encodeFileToBase64(audioFile);
        long audioLength = audioFile.length(); // 原始音频字节数（必填）
        
        // 3. 获取有效的Access Token（缓存+过期校验）
        String validToken = getValidAccessToken();
        
        // 4. 构造请求体（Access Token 填入token字段）
        JSONObject requestBody = new JSONObject();
        requestBody.put("format", "wav"); // 音频格式
        requestBody.put("rate", 16000); // 采样率（固定16000）
        requestBody.put("channel", 1); // 单声道（必填）
        requestBody.put("cuid", macAddress); // 用户唯一标识（MAC地址）
        requestBody.put("dev_pid", 1537); // 普通话模型（有标点）
        requestBody.put("speech", base64Audio); // Base64编码的音频
        requestBody.put("len", audioLength); // 原始音频字节数
        requestBody.put("token", validToken); // 核心：填入Access Token（鉴权关键）
        
        // 5. 调用API，获取转写结果
        return callAsrApi(requestBody);
    }
    
    /**
     * 获取有效的Access Token（缓存+过期校验）
     */
    private String getValidAccessToken() {
        // 检查Token是否有效（未获取 或 已过期）
        if (accessToken == null || System.currentTimeMillis() > tokenExpireTime) {
            synchronized (this) { // 双重检查锁，避免并发重复请求
                if (accessToken == null || System.currentTimeMillis() > tokenExpireTime) {
                    String tokenUrl = String.format(ACCESS_TOKEN_URL, apiKey, secretKey);
                    HttpResponse response = HttpRequest.get(tokenUrl).execute();
                    JSONObject tokenJson = JSONObject.parseObject(response.body());
                    
                    // 解析Token和过期时间（expires_in单位：秒）
                    accessToken = tokenJson.getString("access_token");
                    int expiresIn = tokenJson.getIntValue("expires_in"); // 通常是2592000秒（30天）
                    tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000; // 提前5分钟过期，避免临界问题
                    
                    logger.info("获取Access Token成功，有效期至：{}", tokenExpireTime);
                }
            }
        }
        return accessToken;
    }
    
    /**
     * 本地文件转 Base64
     * @param file 文件对象
     * @return Base64编码字符串
     * @throws Exception 文件读取异常
     */
    private String encodeFileToBase64(File file) throws Exception {
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        }
        return Base64.encodeBase64String(fileBytes);
    }
    
    /**
     * 调用短语音识别API
     * @param requestBody 请求体
     * @return 识别结果文本
     */
    private String callAsrApi(JSONObject requestBody) {
        HttpResponse response = HttpRequest.post(ASR_URL)
                .header("Content-Type", "application/json") // JSON方式固定header
                // 注意：Access Token鉴权时，无需Authorization Header！
                .body(requestBody.toJSONString())
                .execute();
        
        JSONObject resultJson = JSONObject.parseObject(response.body());
        logger.info("百度ASR接口返回：{}", resultJson);
        
        if (resultJson.getIntValue("err_no") == 0) {
            // 识别成功，返回result数组的第一个结果（最优解）
            String result = resultJson.getJSONArray("result").getString(0);
            logger.info("语音识别成功：{}", result);
            return result;
        } else {
            throw new RuntimeException("语音转写失败：" + resultJson.getString("err_msg") + 
                    "（错误码：" + resultJson.getIntValue("err_no") + "）");
        }
    }
}