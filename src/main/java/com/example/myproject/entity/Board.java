package com.example.myproject.entity;

//import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "Board")
public class Board {

    //@jakarta.persistence.Id
    @javax.persistence.Id
    @Id //PK를 의미
    @GeneratedValue(strategy = GenerationType.IDENTITY) //주 키의 값을 데이터베이스가 자동으로 생성하도록 설정
    private int seq;

    @Column(length = 255, nullable = false)
    private String id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(length = 100)
    private String auth;

    private Integer count;

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn = "Y"; // 기본값 'Y'

    @Column(name = "create_date", nullable = false, updatable = false, insertable = false)
    private Timestamp createDate;

    @Column(name = "update_date", nullable = false, updatable = true, insertable = false)
    private Timestamp updateDate;

    @Column(name = "image_path")
    private String imagePath; // 이미지 경로를 저장할 필드

    public Board(String title, String content, String userId, String imagePath, Timestamp timestamp) {
    }

    public Board() {

    }

    //***************************************************************************

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuth() {
        return auth;
    }

    public Integer getCount() {
        return count;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    // useYn 필드의 getter
    public String getUseYn() {
        return useYn;
    }

    public int getSeq() { return seq;}

    public String getImagePath() { return imagePath;}

    //*********************************************************************************************

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public void setUpdateDate(Timestamp updateDate) { this.updateDate = updateDate; }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public void setImagePath(String imagePath) { this.imagePath = imagePath;}

    public void setSeq(int seq) { this.seq = seq; }

}