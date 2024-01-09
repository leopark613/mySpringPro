package com.example.myproject.controller;

import com.example.myproject.entity.User;
import com.example.myproject.service.AuthenticationService;
import com.example.myproject.service.BoardService;
import com.example.myproject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthenticationService authenticationService;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            String token = authenticationService.authenticate(user.getId(), user.getPassword());
            if (token != null) {
                return ResponseEntity.ok().body(new TokenResponse(token));
            } else {
                return ResponseEntity.status(401).body("토큰이 null 이므로 인증 실패");
            }
        } catch (RuntimeException e) {
            logger.error("비밀번호 혹은 아이디 틀림: ", e);
            //아이디나 비밀번호가 틀렸을 경우 무엇이 틀렸는지 구분하여 반환하는 예외. 
            //authenticationService에 로직 구성해놨음
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            //예상치못한 예외
            logger.error("예상치 못한 예외 : ", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
    // 사용자 생성 API 엔드포인트
    @PostMapping("/creatuser")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // UserService 인스턴스를 통해 createUser 호출
            User createdUser = userService.createUser(user.getId(),
                    user.getPassword(),
                    user.getName(),
                    user.getEmail(),
                    user.getAuth(),
                    user.getPhone(),
                    user.getAddress());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("User creation failed: " + e.getMessage());
        }
    }


    // [회원가입] - 사용자 ID 중복 확인
    @GetMapping("/check-duplicate-id")
    public ResponseEntity<?> checkDuplicateId(@RequestParam String id) {
        try {
            boolean isDuplicate = userService.isIdDuplicate(id);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }


    // [회원가입] - 이메일 중복 확인
    @GetMapping("/check-duplicate-email")
    public ResponseEntity<?> checkDuplicateEmail(@RequestParam String email) {
        try {
            boolean isDuplicate = userService.isEmailDuplicate(email);
            return ResponseEntity.ok(isDuplicate);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }


    // TokenResponse 클래스 정의
    private static class TokenResponse {
        private String token;

        public TokenResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
