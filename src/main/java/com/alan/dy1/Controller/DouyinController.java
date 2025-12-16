package com.alan.dy1.Controller;

import com.alan.dy1.Service.AudioConversionService;
import com.alan.dy1.Service.GetWorkCountService;
import com.alan.dy1.Service.SpeechRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/douyin")
public class DouyinController {

    private static final String PYTHON_SCRIPT_PATH = "douyin_tools/get_works_count_from_url.py";

    @Autowired
    private GetWorkCountService getWorkCountService;
    
    @Autowired
    private AudioConversionService audioConversionService;
    
    @Autowired
    private SpeechRecognitionService speechRecognitionService;

    @GetMapping("/works-count")
    public ResponseEntity<Map<String, Object>> getUserWorksCount(@RequestParam("url") String url) {
        return getWorkCountService.getUserWorksCount(url);
    }
    
    @PostMapping("/convert-audio")
    public ResponseEntity<Map<String, Object>> convertAudioFiles(@RequestParam(value = "sourceDir", defaultValue = "douyin_tools/audio") String sourceDir) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            // 获取目录下的所有MP3文件并逐一转换
            java.io.File dir = new java.io.File(sourceDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new IOException("指定的目录不存在或不是一个目录: " + sourceDir);
            }
            
            java.io.File[] mp3Files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".mp3"));
            if (mp3Files == null || mp3Files.length == 0) {
                response.put("success", true);
                response.put("message", "目录中没有找到MP3文件");
                return ResponseEntity.ok(response);
            }
            
            // 转换每个MP3文件
            for (java.io.File mp3File : mp3Files) {
                audioConversionService.convertMp3ToWav(sourceDir, mp3File.getName());
            }
            
            response.put("success", true);
            response.put("message", "音频文件转换完成，共处理了 " + mp3Files.length + " 个文件");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "音频转换失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/recognize-speech")
    public ResponseEntity<Map<String, Object>> recognizeSpeech(@RequestParam("audioFile") String audioFilePath) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String result = speechRecognitionService.recognizeSpeech(audioFilePath);
            response.put("success", true);
            response.put("text", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "语音识别失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}