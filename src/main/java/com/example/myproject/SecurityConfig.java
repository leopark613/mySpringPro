package com.example.myproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    // SecurityFilterChain을 사용하여 보안 설정을 정의합니다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF 보호를 비활성화합니다.
                .authorizeRequests()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // Swagger UI 경로 허용
                .antMatchers("/css/**", "/js/**").permitAll() // CSS 및 JavaScript 파일에 대한 접근을 허용합니다.
                .antMatchers("/view/**").permitAll() // '/view/**' 경로에 대한 접근을 허용합니다.
                .antMatchers("/board/**").permitAll() // '/board/**' 경로에 대한 접근을 허용합니다.
                .antMatchers("/auth/**").permitAll() // '/auth/**' 경로에 대한 접근을 허용합니다.
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger UI 접근 허용
                .antMatchers("/api/**").permitAll()
                .antMatchers("/test/logging").permitAll()   // test용
                .anyRequest().authenticated() // 나머지 모든 요청은 인증을 필요로 합니다.
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패 시 처리를 위해 CustomAuthenticationEntryPoint를 사용합니다.
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않습니다 (STATELESS).
                .and()
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class); // JwtTokenFilter를 UsernamePasswordAuthenticationFilter 이전에 적용합니다.

        return http.build();
    }

    // BCryptPasswordEncoder를 사용하는 PasswordEncoder Bean을 정의합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
