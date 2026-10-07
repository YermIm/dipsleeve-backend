package com.dipsleeve.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 모든 API 응답을 감싸는 공통 틀. 성공과 실패를 이 클래스 하나로 응답.
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    // 데이터가 있는 성공 응답
    // static 메서드는 클래스의 <T>를 쓸 수 없어서 따로 선언 필요
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, null, data);
    }

    // 데이터가 없는 성공 응답
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null, null);
    }

    // 실패 응답
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
