package com.example.myproject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//Spring Security의 커스텀 인증 진입점(Custom Authentication Entry Point)
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 여기에서 응답을 커스터마이즈할 수 있습니다.
        // 예를 들어, 특정 상태 코드를 설정하거나 사용자에게 메시지를 보낼 수 있습니다.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 실패 메시지");
    }
}