package com.dipsleeve.global.exception;

import com.dipsleeve.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 모든 컨트롤러에서 던져진 예외를 한곳에서 잡아 ApiResponse 형식으로 응답하는 처리기.
// @Slf4j: 로그를 남기는 log 객체를 만들어 준다 (log.error(...))
@Slf4j
@RestControllerAdvice
// ResponseEntityExceptionHandler를 상속한 이유:
// 스프링이 원래 처리하던 에러(잘못된 JSON, 없는 주소, 파라미터 오류 등)를
// 부모가 알맞은 상태코드(400, 404)로 처리해 준다.
// 상속하지 않고 Exception을 한 번에 잡으면 이런 에러까지 전부 500이 되어 버린다
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 서비스에서 우리가 던진 예외: 담겨 온 에러코드 그대로 응답한다
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException e) {
        return toResponse(e.getErrorCode());
    }

    // 맨 마지막 안전망: 위에서 처리하지 못한 모든 예외 (버그, DB 장애 등)
    // 원인은 서버 로그에만 남기고, 사용자에게는 일반적인 메시지만 보여준다
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        log.error("예상하지 못한 예외 발생", e);
        return toResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    // @Valid 입력값 검증에 실패한 경우 (요청 본문)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return toResponse(CommonErrorCode.INVALID_INPUT);
    }

    // @Valid 입력값 검증에 실패한 경우 (요청 파라미터)
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return toResponse(CommonErrorCode.INVALID_INPUT);
    }

    // 부모가 처리하는 나머지 스프링 기본 에러가 응답을 만들 때 거치는 길목.
    // 부모는 자기 형식(ProblemDetail)으로 응답하는데, 여기서 ApiResponse 형식으로 바꾼다
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("프레임워크 내부 오류", ex);
            return toResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (statusCode.value() == 404) {
            return toResponse(CommonErrorCode.NOT_FOUND);
        }
        // 그 외 클라이언트 오류(깨진 JSON, 허용 안 된 메서드 등)는 컨벤션의 상태코드에 맞춰 400으로 통일
        return toResponse(CommonErrorCode.INVALID_REQUEST);
    }

    // 에러코드 하나를 받아 ApiResponse 응답으로 바꾸는 공통 메서드
    private ResponseEntity<Object> toResponse(BaseErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }
}
