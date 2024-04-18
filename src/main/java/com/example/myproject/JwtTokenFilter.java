package com.example.myproject;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//jwt 토큰 만료시 여기서 필터링하고 재입장해야하는 검증 로직
@Component
public class JwtTokenFilter extends OncePerRequestFilter {


    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            // JWT 토큰 검증 로직
            // 토큰이 유효하면 필터 체인을 계속 진행
        } catch (ExpiredJwtException e) {
            // JWT 토큰이 만료된 경우 처리
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 토큰이 만료되었습니다.");
            return;
        } catch (JwtException e) {
            // 기타 JWT 관련 예외 처리
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 오류가 발생했습니다.");
            return;
        }
        chain.doFilter(request, response);
    }
}
