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
 * 语音识别服务实现类
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
    
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";
    private static final String ASR_URL = "http://vop.baidu.com/server_api";
    
    @Override
    public String recognizeSpeech(String audioFilePath) throws Exception {
        // 1. 获取 access_token（鉴权凭证，有效期30天）
        String accessToken = getAccessToken();
        
        // 2. 读取本地音频文件（WAV格式，16k采样率、单声道、16bit位深）
        File audioFile = new File(audioFilePath);
        
        // 3. 音频文件转 Base64 编码（JSON方式要求）
        String base64Audio = encodeFileToBase64(audioFile);
        long audioLength = audioFile.length(); // 原始音频字节数（必填）
        
        // 4. 构造请求体（按文档要求传参）
        JSONObject requestBody = new JSONObject();
        requestBody.put("format", "wav"); // 音频格式（和本地文件一致）
        requestBody.put("rate", 16000); // 采样率（固定16000）
        requestBody.put("channel", 1); // 单声道（必填）
        requestBody.put("cuid", "your_mac_address"); // 任意唯一标识（如MAC地址，60字符）
        requestBody.put("token", accessToken); // 鉴权凭证
        requestBody.put("dev_pid", 1537); // 普通话模型（有标点）
        requestBody.put("speech", base64Audio); // Base64编码的音频
        requestBody.put("len", audioLength); // 原始音频字节数
        
        // 5. 调用API，获取转写结果
        return callAsrApi(requestBody);
    }
    
    /**
     * 获取 access_token
     * @return access_token字符串
     */
    private String getAccessToken() {
        String url = String.format(ACCESS_TOKEN_URL, apiKey, secretKey);
        HttpResponse response = HttpRequest.get(url).execute();
        return JSONObject.parseObject(response.body()).getString("access_token");
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
                .body(requestBody.toJSONString())
                .execute();
        
        JSONObject resultJson = JSONObject.parseObject(response.body());
        if (resultJson.getIntValue("err_no") == 0) {
            // 识别成功，返回result数组的第一个结果（最优解）
            return resultJson.getJSONArray("result").getString(0);
        } else {
            throw new RuntimeException("转写失败：" + resultJson.getString("err_msg") + "（err_no：" + resultJson.getIntValue("err_no") + "）");
        }
    }
}
