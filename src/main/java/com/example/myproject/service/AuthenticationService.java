package com.example.myproject.service;

import com.example.myproject.entity.User;
import com.example.myproject.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;

@Service
public class AuthenticationService {

    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey; // @Value 어노테이션을 통해 secretKey 주입

    public AuthenticationService() {
        // 서버 시작 시 SECRET_KEY 값 출력
        System.out.println("Current SECRET_KEY: " + secretKey);

        //logger.info("Secret Key: {}" + secretKey);
        //System.getenv().forEach((k, v) -> logger.info("Env var: {} = {}" + k + v));
    }

    public String authenticate(String username, String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("비밀번호를 입력해주세요");
        }

        User user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 틀립니다");
        }

        System.out.println("로그인 성공");
        return generateToken(user);
    }

    private String generateToken(User user) {
        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .claim("userId", user.getId())
                .claim("auth", user.getAuth())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 86400000)) // 1일 후 만료
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
            return claims.get("userId", String.class);
        } catch (ExpiredJwtException e) {
            logger.log(Level.SEVERE, "JWT 토큰이 만료되었습니다: " + e.getMessage());
            return null;
        }
    }

    public String getAuthFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
            return claims.get("auth", String.class);
        } catch (ExpiredJwtException e) {
            logger.log(Level.SEVERE, "JWT 토큰이 만료되었습니다: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            System.out.println("Received token: " + token);
            token = token.trim(); // 공백 제거
            token = token.replace("Bearer ", "");
            System.out.println("Token after replacing Bearer: " + token);
            System.out.println("Attempting to validate token with key: " + new String(secretKey.getBytes(StandardCharsets.UTF_8)));
            Jwts.parser().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token);
            logger.log(Level.INFO, "JWT 토큰 검증 성공: {0}", token);
            return true;
        } catch (SignatureException ex) {
            logger.log(Level.SEVERE, "JWT 토큰 서명이 일치하지 않습니다: {0}", ex.getMessage());
            return false;
        } catch (ExpiredJwtException ex) {
            logger.log(Level.SEVERE, "JWT 토큰이 만료되었습니다: {0}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "JWT 토큰 검증 실패: {0}", ex.getMessage());
            return false;
        }
    }
}
