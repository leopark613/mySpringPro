package com.example.myproject.service;

import com.example.myproject.entity.User;
import com.example.myproject.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //private final String SECRET_KEY = "your_secret_key"; // 비밀키 설정
    //@Value("${jwt.secret}")
    //private String SECRET_KEY;
    private final String SECRET_KEY;

    public AuthenticationService() {
        //실제 운영시 비밀키를 강력하게 랜덤으로 생성해버리기 - 서버를 껐다 켜버리면 이전에 생성된 토큰은 사용불가
        //SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // 자동 생성
        //this.SECRET_KEY = Base64.getEncoder().encodeToString(key.getEncoded());

        //로컬에서 테스트시, 서버 껐다 켰다 해도 이 해당 하드코딩된 비밀키를 인증하여 JWT토큰 사용하게끔 하기위해 임시방편
        this.SECRET_KEY = "aB3jK9mZ5xH1fG4lQ7pV0sW2uT8yD6rE9cN4bM3jP2oL5kH8gF1dS6aR7eY9wU0xQ5tZ3vH2jM1lK4nO3pE2qA6s";
    }

    public String authenticate(String username, String password) {
        // 비밀번호가 비어있는지 확인
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("비밀번호를 입력해주세요");
        }
        // 아이디가 존재하지 않을 경우
        User user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 비밀번호가 틀릴 경우
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 틀립니다");
        }

        System.out.println("로그인 성공");
        return generateToken(user);
    }
    //jwt 토큰 생성
    private String generateToken(User user) {
        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                //.setSubject(user.getId()) - JWT 표준에서 sub (subject) 클레임이 일반적으로 사용자를 식별하는 데 사용
                .claim("userId", user.getId()) // 사용자 id 추출
                .claim("auth", user.getAuth()) // 사용자 권한 추출
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 86400000)) // 1일 후 만료
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }
    // JWT 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return claims.get("userId", String.class);
    }

    // JWT 토큰에서 사용자 권한 추출
    public String getAuthFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return claims.get("auth", String.class);
    }
}
