package com.alan.dy1.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "douyin")
public class Douyin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "url", nullable = false, length = 255)
    private String url;
    
    @Column(name = "dy_name", nullable = false, length = 225)
    private String name;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "relevant", nullable = false)
    private Boolean relevant;
    
    @Column(name = "work_number", nullable = false)
    private Integer workNumber;
    
    // Constructors
    public Douyin() {}
    
    public Douyin(String url, String name, Integer quantity, Boolean relevant, Integer workNumber) {
        this.url = url;
        this.name = name;
        this.quantity = quantity;
        this.relevant = relevant;
        this.workNumber = workNumber;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Boolean getRelevant() {
        return relevant;
    }
    
    public void setRelevant(Boolean relevant) {
        this.relevant = relevant;
    }
    
    public Integer getWorkNumber() {
        return workNumber;
    }
    
    public void setWorkNumber(Integer workNumber) {
        this.workNumber = workNumber;
    }
    
    @Override
    public String toString() {
        return "Douyin{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", relevant=" + relevant +
                ", workNumber=" + workNumber +
                '}';
    }
}