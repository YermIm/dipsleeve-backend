package com.dipsleeve.global.exception;

import org.springframework.http.HttpStatus;

// 도메인별 Enum이 이걸 구현하면,
// 예외 처리기는 도메인을 몰라도 이 3가지만 꺼내서 응답을 만들 수 있음
public interface BaseErrorCode {

    // 응답의 HTTP 상태코드
    HttpStatus getHttpStatus();

    // 에러 구분용 코드
    String getCode();

    // 사용자에게 보여줄 메시지
    String getMessage();
}
