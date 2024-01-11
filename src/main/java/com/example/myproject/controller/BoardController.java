package com.example.myproject.controller;

import com.example.myproject.entity.Board;
import com.example.myproject.service.BoardService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    // 모든 게시글 조회
    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        List<Board> boards = boardService.findAllBoards();
        return ResponseEntity.ok(boards);
    }

    // 모든 게시글 조회('Y'값만)
//    @GetMapping("/active") //여긴 정상 조회됨. 전체조회임f
//    public ResponseEntity<List<Board>> getActiveBoards() {
//        List<Board> activeBoards = boardService.getActiveBoards();
//        if (activeBoards.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok(activeBoards);
//    }

    @GetMapping("/active")
    public ResponseEntity<List<Board>> getActiveBoards(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String term) {

        List<Board> activeBoards = boardService.searchActiveBoards(type, term);

        if (activeBoards.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activeBoards);
    }



    // 게시글 상세보기 ID로 조회 - 1
//    @GetMapping("/{id}")
//    public ResponseEntity<Board> getBoardById(@PathVariable String id) {
//        Optional<Board> board = boardService.findBoardById(id);
//        return board.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }

    // 게시글 상세보기 ID로 조회 -2 조회수는 증가하나 중복됨(새로고침시 무한증가)
//    @GetMapping("/{id}")
//    public ResponseEntity<Board> getBoardById(@PathVariable String id) {
//        Optional<Board> boardOptional = boardService.findBoardById(id);
//
//        return boardOptional.map(board -> {
//            Board updatedBoard = boardService.incrementCountAndSave(board);
//            return ResponseEntity.ok(updatedBoard);
//        }).orElseGet(() -> ResponseEntity.notFound().build());
//    }

    // 게시글 상세보기 ID로 조회 - 3 조회수증가를 세션으로 처리 (이게 사실 FM)
//    @GetMapping("/{id}")
//    public ResponseEntity<Board> getBoardById2(@PathVariable String id, HttpSession session) {
//        Set<String> viewedBoardIds = (Set<String>) session.getAttribute("viewedBoardIds");
//        if (viewedBoardIds == null) {
//            viewedBoardIds = new HashSet<>();
//        }
//
//        Optional<Board> boardOptional = boardService.findBoardById(id);
//        if (boardOptional.isPresent() && !viewedBoardIds.contains(id)) {
//            boardService.incrementCountAndSave(boardOptional.get());
//            viewedBoardIds.add(id);
//            session.setAttribute("viewedBoardIds", viewedBoardIds);
//        }
//
//        return boardOptional
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    // 게시글 상세보기 ID로 조회 - 4 조회수증가를 세션으로 처리
    // 목록에서만 클릭했을때 조회수 증가하고, 같은 동일한글 또 클릭시 증가 안함. 즉, 다른 글을 클릭해야만 조회수 증가
    @GetMapping("/seq/{seq}")
    public ResponseEntity<Board> getBoardBySeq(@PathVariable int seq, HttpSession session) {
        String lastViewedBoardSeq = (String) session.getAttribute("lastViewedBoardSeq");

        Optional<Board> boardOptional = boardService.findBoardBySeq(seq);

        if (boardOptional.isPresent()) {
            // 목록에서 다시 게시글을 클릭했을 경우 조회수 증가
            if (lastViewedBoardSeq == null || !lastViewedBoardSeq.equals(String.valueOf(seq))) {
                boardService.incrementCountAndSave(boardOptional.get());
                session.setAttribute("lastViewedBoardSeq", String.valueOf(seq)); // 세션에 현재 게시글 seq 저장
            }
        }

        return boardOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //게시물 추가(글쓰기)
    @PostMapping("/create")
    public Board createBoard(@RequestBody Board board, HttpServletRequest request) {
        //insert시 jwt 토큰에서 추출한 auth 함께 저장하기 위함
        String jwtToken = request.getHeader("Authorization").substring(7); // "Bearer " 제거
        return boardService.createBoard(board, jwtToken);
    }
    //게시물 수정
    @PostMapping("/update")
    // ResponseEntity - Spring Web에서 HTTP 요청에 대한 응답
    public ResponseEntity<?> updateBoard(@ModelAttribute Board board, HttpServletRequest request) {
        try {
            //Optional : null을 허용하지않는 목적. 객체가 null일 수도 있다는 것을 명시적으로 표현하고, 이를 안전하게 처리할 수 있는 방법 제공. 즉, NullPointerException 방지
            Optional<Board> existingBoardOpt = boardService.findBoardBySeq(board.getSeq());
            if (existingBoardOpt.isPresent()) {
                Board existingBoard = existingBoardOpt.get();
                existingBoard.setTitle(board.getTitle());
                existingBoard.setContent(board.getContent());
                existingBoard.setUpdateDate(new Timestamp(System.currentTimeMillis())); // 수정 날짜 업데이트
                boardService.saveOrUpdateBoard(existingBoard); // 변경된 내용 저장
                
                //리다이렉트 로직
                HttpHeaders headers = new HttpHeaders();
                //저장 업데이트 이후에 다시 상세페이지(detail.html)로 이동할때 쿼리스트링으로 id,seq을 갖고 넘어가게 하기
                headers.add("Location", "/view/detail.html?id=" + existingBoard.getId() + "&seq=" + existingBoard.getSeq());
                    //ResponseEntity<String> : 응답 본문이 문자열이 될 것임을 나타냄
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //삭제(상태값 업데이트 'Y' -> 'N')
    @PostMapping("/deactivate/{seq}")
    public RedirectView deactivateBoard(@PathVariable int seq) {
        boardService.deactivateBoard(seq);
        //return ResponseEntity.ok().build(); // 단순한 성공 응답 반환
        // board.html로 리디렉션
        return new RedirectView("/view/board.html");
    }





//    // 게시글 저장 또는 업데이트
//    @PostMapping
//    public ResponseEntity<Board> saveOrUpdateBoard(@RequestBody Board board) {
//        Board savedBoard = boardService.saveOrUpdateBoard(board);
//        return ResponseEntity.ok(savedBoard);
//    }

    // 게시글 ID로 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoardById(@PathVariable String id) {
        boardService.deleteBoardById(id);
        return ResponseEntity.ok().build();
    }
}
