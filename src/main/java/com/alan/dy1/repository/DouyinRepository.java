package com.alan.dy1.repository;

import com.alan.dy1.domain.Douyin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DouyinRepository extends JpaRepository<Douyin, Integer> {
    Optional<Douyin> findByUrl(String url);
    
    // 获取所有抖音数据对象
    List<Douyin> findAll();
}