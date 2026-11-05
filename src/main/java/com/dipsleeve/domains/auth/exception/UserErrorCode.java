package com.dipsleeve.domains.auth.exception;

import com.dipsleeve.global.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 사용자(user) 관련 에러코드. 코드 형식: USER_상태코드_번호
// 공통 에러는 CommonErrorCode에 있고, 이 Enum은 사용자 데이터에 관련된 에러만 모은다.
// BaseErrorCode를 구현하므로 GlobalExceptionHandler가 도메인을 몰라도 응답을 만들 수 있다
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    // 409: 이미 존재하는 값과 충돌
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER_409_1", "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER_409_2", "이미 사용 중인 닉네임입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
