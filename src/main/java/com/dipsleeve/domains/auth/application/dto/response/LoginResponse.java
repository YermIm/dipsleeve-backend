package com.dipsleeve.domains.auth.application.dto.response;

// 로그인 성공 시 내보내는 값
// accessToken: 발급한 JWT, tokenType: 토큰을 보낼 때 앞에 붙이는 종류("Bearer"), expiresIn: 유효 시간(초)
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    // tokenType은 항상 "Bearer"이므로 호출하는 쪽에서 매번 쓰지 않도록 고정
    public static LoginResponse of(String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, "Bearer", expiresIn);
    }
}
