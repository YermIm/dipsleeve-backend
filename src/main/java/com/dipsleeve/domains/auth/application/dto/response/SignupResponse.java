package com.dipsleeve.domains.auth.application.dto.response;

import com.dipsleeve.domains.auth.domain.entity.User;

// 회원가입 성공 시 data에 담겨 나가는 객체. 비밀번호는 일부러 넣지 않음.
public record SignupResponse(Long id, String email, String nickname) {

    // 엔티티를 응답 객체로 바꾸는 통로. 사용: SignupResponse.from(user)
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
