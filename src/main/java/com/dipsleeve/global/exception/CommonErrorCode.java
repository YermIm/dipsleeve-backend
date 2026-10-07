package com.dipsleeve.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 도메인과 상관없이 어디서든 생길 수 있는 공통 에러.
// 코드 형식: 영역_HTTP상태코드_번호 (예: COMMON_400_1)
//   - 영역: 에러가 속한 도메인 (공통은 COMMON)
//   - 번호: 같은 영역, 같은 상태코드 안에서의 일련번호
// enum은 값의 종류가 정해진 클래스라서, 아래 항목이 곧 이 클래스의 객체들.
@Getter
@RequiredArgsConstructor // final 필드를 받는 생성자 (아래 항목 괄호 안의 3개 값이 순서대로 들어간다)
public enum CommonErrorCode implements BaseErrorCode {

    // 400: 요청이 잘못된 경우
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400_1", "잘못된 요청입니다."),
    // @Valid 입력값 검증에 실패한 경우
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400_2", "입력값이 올바르지 않습니다."),

    // 500: 예상하지 못한 서버 오류 (이 상태코드는 이 용도로만 쓴다)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_1", "서버 내부 오류가 발생했습니다."),
    ; // 항목 목록의 끝을 표시하는 세미콜론 (마지막 항목 뒤에 쉼표와 함께 둬도 된다)

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
