package com.dipsleeve.domains.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

// 로그인 요청에서 받는 값: 이메일과 비밀번호
// 빈 값만 막고, 형식(@Email)이나 길이(@Size)는 일부러 검사하지 않는다.
// 형식 검사는 "이 이메일은 규칙에 맞지 않는다"는 힌트를 주고, 어차피 틀리면 로그인 실패(401)로 처리하기 때문이다
public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
