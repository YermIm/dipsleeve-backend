package com.dipsleeve.domains.auth.exception;

import com.dipsleeve.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 인증(auth) 관련 에러코드. 코드 형식: AUTH_상태코드_번호
// 로그인, 토큰 등 "누구인지 확인하는 과정"에서 생기는 에러만 모은다.
// 사용자 데이터 자체의 에러(이메일 중복 등)는 UserErrorCode에 있다
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 401: 인증 실패. 이메일 없음과 비밀번호 불일치를 구분하지 않고 같은 코드로 응답한다
    // (구분하면 가입된 이메일인지 알아낼 수 있기 때문)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "이메일 또는 비밀번호가 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
