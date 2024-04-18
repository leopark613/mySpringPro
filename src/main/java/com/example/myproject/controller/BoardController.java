package com.example.myproject.controller;

import com.example.myproject.entity.Board;
import com.example.myproject.service.AuthenticationService;
import com.example.myproject.service.BoardService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpSession;
import com.example.myproject.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private AuthenticationService authenticationService;

    // 모든 게시글 조회
    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards(@RequestHeader(name = "Authorization") String authToken) {
        if (!authenticationService.isTokenValid(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
            @RequestHeader(name = "Authorization") String authToken,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String term) {
            System.out.println("authToken1 : "+ authToken);
        if (!authenticationService.isTokenValid(authToken)) {
            System.out.println("authToken2 : "+ authToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Board> activeBoards = boardService.searchActiveBoards(type, term);
        System.out.println("authToken3 : "+ authToken);
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
    public ResponseEntity<Board> getBoardBySeq(
            @PathVariable int seq,
            HttpSession session,
            @RequestHeader(name = "Authorization") String authToken) {
        //토큰 검증 - 하단 authenticationService.isTokenValid 에서 공백제거가 안되므로 여기서 제거시킴
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7).trim();  // "Bearer "를 제거하고 공백을 제거
        }

        if (!authenticationService.isTokenValid(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return boardService.findBoardBySeq(seq)
                .map(board -> {
                    boardService.incrementCountAndSave(board);
                    return ResponseEntity.ok(board);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 게시글쓰그는 화면
    @PostMapping("/writeBoard")
    public ResponseEntity<Map<String, Boolean>> writeBoardPage(@RequestHeader("Authorization") String authToken) {
        boolean isValid = authenticationService.isTokenValid(authToken);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return ResponseEntity.ok(response);
    }


    // 게시물 추가 (글쓰기 - 저장)
    @PostMapping("/create")
    public ResponseEntity<Board> createBoard(
            @RequestBody Board request,
            @RequestHeader(name = "Authorization") String authToken,
            HttpServletRequest httpServletRequest) throws IOException {

        //토큰 검증 - 하단 authenticationService.isTokenValid 에서 공백제거가 안되므로 여기서 제거시킴
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7).trim();  // "Bearer "를 제거하고 공백을 제거
        }
        if (!authenticationService.isTokenValid(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authenticationService.getUserIdFromToken(authToken);  // 사용자 ID 추출
        //String imagePath = image != null ? saveImage(image, request) : null;  // 이미지 저장 및 경로 추출

        Board savedBoard = boardService.createBoard(request.getTitle(), request.getContent(), userId, null, authToken);  // 서비스 메서드로 파라미터 전달
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBoard);
    }


    // 수정하기 화면
    @PostMapping("/updatePage")
    public ResponseEntity<Map<String, Boolean>> updateDetailPage(@RequestHeader("Authorization") String authToken) {
        boolean isValid = authenticationService.isTokenValid(authToken);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return ResponseEntity.ok(response);
    }

    // 게시물 수정
    @PostMapping("/update")
    public ResponseEntity<?> updateBoard(
            @RequestBody Board board,
            @RequestHeader(name = "Authorization") String authToken,
            HttpServletRequest request) {

        if (!authenticationService.isTokenValid(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Optional<Board> existingBoardOpt = boardService.findBoardBySeq(board.getSeq());
            if (existingBoardOpt.isPresent()) {
                Board existingBoard = existingBoardOpt.get();
                existingBoard.setTitle(board.getTitle());
                existingBoard.setContent(board.getContent());
                existingBoard.setUpdateDate(new Timestamp(System.currentTimeMillis()));

                boardService.saveOrUpdateBoard(existingBoard);
                return ResponseEntity.ok(existingBoard);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 이미지 파일을 저장하고 경로를 반환하는 메소드
    private String saveImage(MultipartFile file, HttpServletRequest request) throws IOException {
        String uploadsDir = "/uploads/";
        String realPathtoUploads = request.getServletContext().getRealPath(uploadsDir);
        if (!new File(realPathtoUploads).exists()) {
            new File(realPathtoUploads).mkdir();
        }
        String orgName = file.getOriginalFilename();
        String filePath = realPathtoUploads + orgName;
        File dest = new File(filePath);
        file.transferTo(dest);
        return request.getContextPath() + uploadsDir + orgName;
    }



    //삭제(상태값 업데이트 'Y' -> 'N')
    @PostMapping("/deactivate/{seq}")
    public RedirectView deactivateBoard(
            @PathVariable int seq,
            @RequestHeader(name = "Authorization") String authToken) {
        if (!authenticationService.isTokenValid(authToken)) {
            return new RedirectView("/login"); // Redirect to login if unauthorized
        }
        boardService.deactivateBoard(seq);
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
    public ResponseEntity<Void> deleteBoardById(
            @PathVariable String id,
            @RequestHeader(name = "Authorization") String authToken) {
        if (!authenticationService.isTokenValid(authToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boardService.deleteBoardById(id);
        return ResponseEntity.ok().build();
    }

    // 이미지 업로드를 처리하는 엔드포인트
    @PostMapping("/upload")
    public ResponseEntity<?> handleImageUpload(@RequestParam("image") MultipartFile file) {
        try {
            // 이미지를 저장하고 웹 접근 가능한 URL을 반환하는 로직
            String imagePath = imageStorageService.storeImage(file);

            // 여기서 imagePath는 웹에서 접근 가능한 URL이어야 합니다.
            return ResponseEntity.ok(Map.of("path", imagePath));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 업로드 실패: " + e.getMessage());
        }
    }
}
