package com.alan.dy1.Service.Impl;

import com.alan.dy1.Service.DouyinDataService;
import com.alan.dy1.domain.Douyin;
import com.alan.dy1.repository.DouyinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DouyinDataServiceImpl implements DouyinDataService {
    
    @Autowired
    private DouyinRepository douyinRepository;
    
    @Override
    public String saveDouyinData(Douyin douyin) {
        douyinRepository.save(douyin);
        return "success";
    }
    
    @Override
    public List<Douyin> getAllDouyinData() {
        return douyinRepository.findAll();
    }
}