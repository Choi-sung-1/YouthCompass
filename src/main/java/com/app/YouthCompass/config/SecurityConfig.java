package com.app.YouthCompass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
//                CSRF 공격 방어 기능 끔 (테스트위함)
                .csrf(csrf->csrf.disable())
//                URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
//                        위의 경로를 제외한 모든 요청은 로그인 사용자만 가능
                        .anyRequest().authenticated()
                )
                .formLogin(form-> form
                        .loginPage("/member/login")
                        .usernameParameter("memberLoginId")
                        .passwordParameter("memberPassword")
                        .loginProcessingUrl("/member/login")
                        .defaultSuccessUrl("/",false)
                        .failureUrl("/member/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

}
