package com.alan.dy1.Controller;

import com.alan.dy1.Service.DouyinDataService;
import com.alan.dy1.domain.Douyin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/douyin-data")
public class DouyinDataController {
    
    @Autowired
    private DouyinDataService douyinDataService;
    
    @PostMapping("/save")
    @ResponseBody
    public String saveDouyinData(@RequestBody Douyin douyin) {
        String result = douyinDataService.saveDouyinData(douyin);
        return result;
    }
    
    @GetMapping("/all")
    @ResponseBody
    public List<Douyin> getAllDouyinData() {
        return douyinDataService.getAllDouyinData();
    }
}