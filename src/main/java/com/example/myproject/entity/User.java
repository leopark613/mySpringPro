package com.example.myproject.entity;

import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.sql.Timestamp;

@Entity
public class User {
    @javax.persistence.Id
    //@jakarta.persistence.Id
    @Id
    private String id;
    private String password;
    private String name;
    private String email;
    private String auth;

    private String phone;

    private String address;

    @Column(name = "create_date", nullable = false, updatable = false, insertable = false)
    private Timestamp createDate;

    @Column(name = "update_date", nullable = false, updatable = true, insertable = false)
    private Timestamp updateDate;
    // 기본 생성자
    public User() {
    }

    // id의 게터와 세터
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // password의 게터와 세터
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // name의 게터와 세터
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // email의 게터와 세터
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // auth의 게터와 세터
    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    // phone의 게터와 세터
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    // address 게터와 세터
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createDate = now;
        updateDate = now; // 최초 생성 시에도 updateDate 설정
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = new Timestamp(System.currentTimeMillis());
    }
}