package com.dipsleeve.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

// JWT 토큰을 만들고(발급), 확인하고(검증), 안의 사용자 정보를 꺼내는(추출) 도구.
// 토큰의 규칙(어떤 키로 서명하는지, 언제 만료되는지)을 이 한 곳에서만 관리한다
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;

    // yaml에서 읽은 설정값(JwtProperties)을 받아, 문자열 비밀 키를 서명용 키 객체로 바꿔 둔다
    public JwtProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
    }

    // 로그인에 성공한 사용자의 id를 담아 Access 토큰을 발급한다
    public String createAccessToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                // subject: 이 토큰의 주인. 사용자 id만 담는다 (비밀번호 같은 민감 정보는 절대 담지 않는다)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration.toMillis()))
                // 비밀 키로 서명한다. 서명이 있어야 내용을 바꾸면 들통난다
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰이 유효한지 확인한다: 서명이 맞고, 만료되지 않았고, 형식이 올바르면 true
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 사용자 id를 꺼낸다. 유효하지 않은 토큰이면 예외가 발생하므로 validateToken으로 먼저 확인한다
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    // 서명을 확인하면서 토큰 내용을 읽는다. 서명이 틀리거나 만료되었거나 형식이 깨졌으면 예외가 발생한다
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
