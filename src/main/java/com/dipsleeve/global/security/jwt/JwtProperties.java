package com.dipsleeve.global.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// 설정 파일(yaml)의 jwt 아래 값을 읽어 담는 객체. 값이 없거나 규칙에 어긋나면 서버가 켜지지 않음
// secret: 토큰에 서명하는 비밀 키 (HS256은 32바이트 이상이어야 안전)
// accessTokenExpiration: Access 토큰 유효 시간. yaml에 1h, 30m처럼 쓰면 Duration으로 바뀜
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret, @NotNull Duration accessTokenExpiration) {}
