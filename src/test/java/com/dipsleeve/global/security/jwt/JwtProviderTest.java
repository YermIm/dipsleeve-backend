package com.dipsleeve.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    // 테스트에서만 쓰는 가짜 키 (32자 이상)
    private static final String SECRET = "test-secret-key-for-jwt-unit-test-0123456789";
    private static final String OTHER_SECRET = "another-secret-key-for-jwt-unit-test-9876543210";

    // 스프링이나 DB 없이 JwtProvider를 직접 만든다 (테스트 빠름)
    private JwtProvider createProvider(String secret, Duration expiration) {
        return new JwtProvider(new JwtProperties(secret, expiration));
    }

    // @Test: 이 메서드가 테스트라는 표시. @DisplayName: 결과 화면에 보일 설명
    @Test
    @DisplayName("발급한 토큰은 유효하고, 같은 사용자 id가 나온다")
    void issueAndRead() {
        // 준비: 1시간짜리 토큰을 발급할 수 있는 도구
        JwtProvider provider = createProvider(SECRET, Duration.ofHours(1));

        // 실행: 42번 사용자의 토큰 발급
        String token = provider.createAccessToken(42L);

        // 확인: 유효하고, 다시 읽으면 42번이 나와야 한다
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("다른 키로 서명한 토큰은 검증에 실패한다")
    void rejectTokenSignedWithOtherKey() {
        String token = createProvider(OTHER_SECRET, Duration.ofHours(1)).createAccessToken(42L);

        assertThat(createProvider(SECRET, Duration.ofHours(1)).validateToken(token))
                .isFalse();
    }

    @Test
    @DisplayName("내용을 바꿔치기한 토큰은 검증에 실패한다")
    void rejectTamperedToken() {
        JwtProvider provider = createProvider(SECRET, Duration.ofHours(1));
        String[] mine = provider.createAccessToken(1L).split("\\.");
        String[] other = provider.createAccessToken(2L).split("\\.");

        // 1번 사용자 토큰의 서명에, 2번 사용자 토큰의 내용을 붙인 위조 토큰
        String forged = mine[0] + "." + other[1] + "." + mine[2];

        assertThat(provider.validateToken(forged)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void rejectExpiredToken() {
        // 유효 시간을 -1초로 하면 발급되자마자 이미 만료된 토큰이 된다
        JwtProvider provider = createProvider(SECRET, Duration.ofSeconds(-1));

        String token = provider.createAccessToken(42L);

        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 검증에 실패한다")
    void rejectMalformedToken() {
        JwtProvider provider = createProvider(SECRET, Duration.ofHours(1));

        assertThat(provider.validateToken("abc.def")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("유효하지 않은 토큰에서 사용자 id를 꺼내면 예외가 발생한다")
    void getUserIdFromInvalidToken() {
        JwtProvider provider = createProvider(SECRET, Duration.ofHours(1));

        assertThatThrownBy(() -> provider.getUserId("abc.def")).isInstanceOf(JwtException.class);
    }
}
