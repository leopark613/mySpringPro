package com.example.myproject.service;

import com.example.myproject.entity.Board;
import com.example.myproject.repository.BoardRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.persistence.PersistenceContext;
//import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 모든 게시글 조회
    public List<Board> findAllBoards() {
        return boardRepository.findAll();
    }

    // 모든 게시글을 조회 하되 'Y' 인 상태값만 조회
    public List<Board> getActiveBoards() {
        // 모든 게시물을 가져와서 useYn이 'Y'인 게시물만 필터링
        return boardRepository.findAll()
                .stream()
                .filter(board -> "Y".equals(board.getUseYn()))
                .collect(Collectors.toList());
    }

    // 게시글 ID로 조회 - 이제 사용안함
    //public Optional<Board> findBoardById(String id) {
        //return boardRepository.findById(id);
    //}

    // 게시글 seq로 조회
    public Optional<Board> findBoardBySeq(int seq) {
        return boardRepository.findBySeq(seq);
    }

    // 조회수 증가 저장 메서드
    public Board incrementCountAndSave(Board board) {
        board.setCount(board.getCount() == null ? 1 : board.getCount() + 1);
        return boardRepository.save(board);
    }


    // 게시글 저장 또는 업데이트
    public Board saveOrUpdateBoard(Board board) {
        return boardRepository.save(board);
        //return boardRepository.updateBoard(board);
    }

    public void deactivateBoard(int seq) {
        Board board = boardRepository.findBySeq(seq)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));
        board.setUseYn("N");
        boardRepository.save(board);
    }

    // 게시글 ID로 삭제
    public void deleteBoardById(String id) {
        boardRepository.deleteById(id);
    }

    //게시글 추가(글쓰기)
    public Board createBoard(Board board) {
        board.setCreateDate(new Timestamp(System.currentTimeMillis()));
        board.setUpdateDate(new Timestamp(System.currentTimeMillis()));
        board.setAuth(String.valueOf(0)); // auth를 0으로 초기화
        board.setCount(0); // count를 0으로 초기화
        return boardRepository.save(board);
    }



    // 게시글 전체조회 or 조건 조회
    @PersistenceContext
    private EntityManager entityManager;
    public List<Board> searchActiveBoards(String type, String term) {
        String queryString = "SELECT b FROM Board b WHERE b.useYn = 'Y'";

        if(type != null && !type.isEmpty() && term != null && !term.isEmpty()) {
            switch (type) {
                case "title":
                    queryString += " AND b.title LIKE :term";
                    break;
                case "content":
                    queryString += " AND b.content LIKE :term";
                    break;
                case "id":
                    queryString += " AND b.id LIKE :term";
                    break;
                default:
                    // 기본 동작
            }
        }
        queryString += " ORDER BY b.createDate DESC";
        TypedQuery<Board> query = entityManager.createQuery(queryString, Board.class);
        if(type != null && !type.isEmpty() && term != null && !term.isEmpty()) {
            query.setParameter("term", "%" + term + "%");
        }
        return query.getResultList();
    }
}
