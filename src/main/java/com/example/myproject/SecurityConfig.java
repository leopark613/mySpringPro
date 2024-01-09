package com.example.myproject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 보호 비활성화
                .authorizeRequests()
                .antMatchers("/css/**", "/js/**").permitAll() // CSS 및 JavaScript 파일에 대한 접근 허용
                .antMatchers("/view/**").permitAll() // '/view/**' 경로에 대한 접근 허용
                .antMatchers("/board/**").permitAll() // '/board/**' 경로는 인증 없이 접근 허용
                .antMatchers("/auth/**").permitAll() // '/auth/**' 경로도 인증 없이 접근 허용
                .antMatchers("/api/**").permitAll() // '/api/login' 경로도 인증 없이 접근 허용
                .anyRequest().authenticated() // 그 외의 모든 요청은 인증 필요
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션 사용 안 함

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // PasswordEncoder Bean 정의
    }
}
