package com.alan.dy1.Service.Impl;

import com.alan.dy1.Service.AudioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class AudioServiceImpl implements AudioService {
    
    private static final Logger logger = LoggerFactory.getLogger(AudioServiceImpl.class);
    private static final String PYTHON_SCRIPT_PATH = "douyin_tools/get_audio_from_url.py";
    
    @Override
    public ResponseEntity<Map<String, Object>> downloadAndConvertAudio(String url) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查Python脚本是否存在
            File scriptFile = new File(PYTHON_SCRIPT_PATH);
            if (!scriptFile.exists()) {
                // 尝试使用绝对路径
                String absolutePath = new File("../" + PYTHON_SCRIPT_PATH).getAbsolutePath();
                scriptFile = new File(absolutePath);
                if (!scriptFile.exists()) {
                    response.put("success", false);
                    response.put("error", "Python脚本未找到: " + PYTHON_SCRIPT_PATH + " 或 " + absolutePath);
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // 构建命令，添加-j参数以JSON格式输出
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("python", scriptFile.getAbsolutePath(), "-u", url, "-j");
            
            // 设置工作目录为脚本所在目录，确保依赖和资源文件可以正确加载
            processBuilder.directory(scriptFile.getParentFile());
            
            // 添加环境变量以支持更好的网络连接
            Map<String, String> environment = processBuilder.environment();
            environment.put("PYTHONIOENCODING", "utf-8");
            
            // 启动进程
            Process process = processBuilder.start();
            
            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 成功执行，解析JSON输出
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(output.toString().trim());
                    
                    response.put("success", jsonNode.get("success").asBoolean());
                    
                    if (jsonNode.has("message")) {
                        response.put("message", jsonNode.get("message").asText());
                    }
                    
                    if (jsonNode.has("file_path")) {
                        String filePath = jsonNode.get("file_path").asText();
                        response.put("file_path", filePath);
                        // 提取文件名并添加到响应中
                        String fileName = new File(filePath).getName();
                        response.put("file_name", fileName);
                    }
                    
                    if (jsonNode.has("error")) {
                        response.put("error", jsonNode.get("error").asText());
                    }
                } catch (Exception e) {
                    // JSON解析失败，返回原始输出
                    response.put("success", true);
                    response.put("message", output.toString());
                }
                return ResponseEntity.ok(response);
            } else {
                // 执行出错
                response.put("success", false);
                response.put("error", "Python脚本执行失败，退出码: " + exitCode);
                response.put("details", errorOutput.toString());
                response.put("command", "python " + scriptFile.getAbsolutePath() + " -u " + url);
                response.put("workingDirectory", scriptFile.getParentFile().getAbsolutePath());
                return ResponseEntity.status(500).body(response);
            }
        } catch (IOException | InterruptedException e) {
            response.put("success", false);
            response.put("error", "执行过程中发生异常: " + e.getMessage());
            response.put("exception", e.getClass().getName());
            return ResponseEntity.status(500).body(response);
        }
    }
}