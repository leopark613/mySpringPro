package com.example.myproject.service;

import com.example.myproject.entity.Board;
import com.example.myproject.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @PersistenceContext
    private EntityManager entityManager;

    // 모든 게시글 조회
    public List<Board> findAllBoards() {
        return boardRepository.findAll();
    }

    // 활성 게시글만 조회
    public List<Board> getActiveBoards() {
        return boardRepository.findAll()
                .stream()
                .filter(board -> "Y".equals(board.getUseYn()))
                .collect(Collectors.toList());
    }

    // 게시글 Seq로 조회
    public Optional<Board> findBoardBySeq(int seq) {
        return boardRepository.findBySeq(seq);
    }

    // 조회수 증가 및 저장
    public Board incrementCountAndSave(Board board) {
        board.setCount(board.getCount() + 1);
        boardRepository.save(board);
        return board;
    }

    // 게시글 생성
    public Board createBoard(String title, String content, String userId, String imagePath, String authToken) {
        String userAuth = authenticationService.getAuthFromToken(authToken); // 권한 정보 추출

        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setId(userId); // 사용자 ID 설정
        board.setImagePath(imagePath); // 이미지 경로 설정
        board.setAuth(userAuth); // 사용자 권한 설정
        board.setCreateDate(new Timestamp(System.currentTimeMillis())); // 생성 날짜 설정
        board.setUpdateDate(new Timestamp(System.currentTimeMillis())); // 수정 날짜 설정
        board.setCount(0); // 조회수 초기화
        return boardRepository.save(board); // DB에 저장
    }


    // 게시글 저장 또는 업데이트
    public Board saveOrUpdateBoard(Board board) {
        return boardRepository.save(board);
    }

    // 게시글 비활성화 (삭제 아님)
    public void deactivateBoard(int seq) {
        Board board = findBoardBySeq(seq).orElseThrow(() -> new EntityNotFoundException("Board not found"));
        board.setUseYn("N");
        boardRepository.save(board);
    }

    // 게시글 ID로 삭제
    public void deleteBoardById(String id) {
        boardRepository.deleteById(id);
    }

    // 게시글 검색
    public List<Board> searchActiveBoards(String type, String term) {
        String queryString = "SELECT b FROM Board b WHERE b.useYn = 'Y'";
        if (type != null && term != null) {
            queryString += switch (type) {
                case "title" -> " AND b.title LIKE :term";
                case "content" -> " AND b.content LIKE :term";
                case "id" -> " AND b.id LIKE :term";
                default -> "";
            };
        }
        queryString += " ORDER BY b.createDate DESC";
        TypedQuery<Board> query = entityManager.createQuery(queryString, Board.class);
        if (type != null && term != null) {
            query.setParameter("term", "%" + term + "%");
        }
        return query.getResultList();
    }
}
