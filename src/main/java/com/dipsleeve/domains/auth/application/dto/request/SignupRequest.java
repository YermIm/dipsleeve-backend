package com.dipsleeve.domains.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 회원가입 요청 본문을 받는 객체. 검증 규칙을 어기면 컨트롤러에 도달하기 전에 400(COMMON_400_2)이 된다.
// record: 값만 담는 불변 클래스를 짧게 쓰는 문법 (필드와 생성자, getter가 자동 생성됨)
public record SignupRequest(
        // @NotBlank: null, 빈 문자열, 공백만 있는 값 모두 불가
        @NotBlank @Email @Size(max = 255) String email,

        // 8~72자, 영문/숫자/특수문자만 (공백과 한글 불가). 72자 제한은 BCrypt가 72바이트까지만 받기 때문이고,
        // ASCII만 허용하면 글자 수와 바이트 수가 같아져서 이 제한이 정확해짐.
        @NotBlank @Size(min = 8, max = 72) @Pattern(regexp = "^[\\x21-\\x7E]+$")
        String password,

        @NotBlank @Size(min = 2, max = 50) String nickname) {}
