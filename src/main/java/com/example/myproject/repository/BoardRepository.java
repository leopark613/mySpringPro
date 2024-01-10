package com.example.myproject.repository;
import com.example.myproject.entity.Board;
//import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, String> {
    // 필요한 메소드를 추가할 수 있습니다.

    List<Board> findByUseYn(String useYn);
    List<Board> findByTitleContainingAndUseYn(String title, String useYn);
    List<Board> findByContentContainingAndUseYn(String content, String useYn);
    List<Board> findByIdContainingAndUseYn(String id, String useYn);

    //@Query("SELECT b FROM Board b ORDER BY b.createDate DESC")
    //List<Board> findAllOrderByCreateDateDesc();

    // seq 값으로 Board 엔티티를 찾는 메소드
    Optional<Board> findBySeq(int seq);
}

