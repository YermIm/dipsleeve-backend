package com.dipsleeve.global.exception;

import lombok.Getter;

// 비즈니스 규칙을 어겼을 때 서비스에서 던지는 예외
// 던질 때 에러코드를 같이 담고, 전역 예외 처리기가 이 에러코드로 응답을 만든다.

// RuntimeException을 상속한 이유:
//   1) 메서드마다 throws를 적지 않아도 된다 (체크 예외와의 차이)
//   2) @Transactional은 기본적으로 RuntimeException이 나면 DB 변경을 되돌린다(롤백)
@Getter
public class BusinessException extends RuntimeException {

    // 어떤 에러인지 담고 있는 에러코드
    private final BaseErrorCode errorCode;

    public BusinessException(BaseErrorCode errorCode) {
        // 부모에 메시지를 넘긴다. 로그나 스택 트레이스에 이 메시지가 찍힌다.
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
