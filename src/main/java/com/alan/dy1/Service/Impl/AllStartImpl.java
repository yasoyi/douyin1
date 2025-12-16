package com.alan.dy1.Service.Impl;

import com.alan.dy1.Service.AllStart;
import com.alan.dy1.Service.AudioConversionService;
import com.alan.dy1.Service.AudioService;
import com.alan.dy1.Service.GetWorkCountService;
import com.alan.dy1.Service.SpeechRecognitionService;
import com.alan.dy1.domain.Douyin;
import com.alan.dy1.repository.DouyinRepository;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AllStartImpl implements AllStart {
    
    private static final Logger logger = LoggerFactory.getLogger(AllStartImpl.class);
    
    @Autowired
    private AudioService audioService;
    
    @Autowired
    private AudioConversionService audioConversionService;
    
    @Autowired
    private SpeechRecognitionService speechRecognitionService;
    
    @Autowired
    private GetWorkCountService getWorkCountService;

    @Autowired
    private DouyinRepository douyinRepository;
    
    @Override
    public void startAllServices() {
        logger.info("开始执行所有服务...");
        
        try {
            // 步骤1: 检查视频数量
            logger.info("步骤1: 检查视频数量");
            List<Douyin> allList = douyinRepository.findAll();

            for (Douyin douyin : allList){
                String url = douyin.getUrl();
                int oldNumber = douyin.getWorkNumber();
                ResponseEntity<Map<String, Object>> response = getWorkCountService.getUserWorksCount(url);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    if (responseBody.containsKey("worksCount")) {

                        Integer worksCount = (Integer) responseBody.get("worksCount");
                        // 业务逻辑处理
                        if (worksCount > oldNumber) {
                            // 处理有新作品的情况

                        } else if (worksCount < oldNumber){
                            // 处理删作品的情况
                            douyin.setWorkNumber(worksCount);
                            douyinRepository.save(douyin);

                        } else if (worksCount == oldNumber){
                            // 处理无新作品情况
                            logger.info("无新作品,结束流程");
                        }
                    }
                }
            }
            
            logger.info("所有服务执行完成");
        } catch (Exception e) {
            logger.error("执行服务过程中出现错误", e);
        }
    }
}