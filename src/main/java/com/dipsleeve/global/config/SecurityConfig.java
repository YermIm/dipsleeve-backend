package com.dipsleeve.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/auth/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated());
        return http.build();
    }

    // 비밀번호를 해시하고 비교하는 도구. 스프링이 하나만 만들어 두고 필요한 곳(회원가입, 로그인)에 넣어 준다.
    // 사용: passwordEncoder.encode("원본")으로 저장용 해시를 만들고,
    //      passwordEncoder.matches("입력값", "저장된 해시")로 로그인 시 일치 여부를 확인한다.
    // 반환 타입을 PasswordEncoder(규격)로 둬서, 나중에 알고리즘을 바꿔도 쓰는 쪽은 고치지 않아도 된다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
