package com.dipsleeve.domains.auth.controller;

import com.dipsleeve.domains.auth.application.dto.request.LoginRequest;
import com.dipsleeve.domains.auth.application.dto.request.SignupRequest;
import com.dipsleeve.domains.auth.application.dto.response.LoginResponse;
import com.dipsleeve.domains.auth.application.dto.response.SignupResponse;
import com.dipsleeve.domains.auth.application.service.AuthService;
import com.dipsleeve.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// HTTP 요청을 받는 입구. 요청을 받아 서비스에 넘기고, 결과를 JSON으로 돌려준다.
// 규칙(중복 확인, 암호화 등)은 여기서 하지 않고 서비스가 맡는다 (컨트롤러는 얇게)
// @RestController: "응답을 JSON으로 돌려주는 컨트롤러"라는 표시이자 스프링 빈 등록 표시
@RestController
// 이 클래스의 모든 경로 앞에 /auth가 붙는다
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /auth/signup 요청을 이 메서드가 처리
    @PostMapping("/signup")
    // 성공 시 상태코드를 기본값 200이 아닌 201(Created, 새로 만들어짐)로 응답한다
    @ResponseStatus(HttpStatus.CREATED)
    // @RequestBody: 요청 본문(JSON)을 SignupRequest 객체로 바꿔 준다
    // @Valid: 바꾼 객체의 검증 규칙(@Email, @Size 등)을 실행한다. 실패하면 이 메서드는 실행되지 않고
    //         GlobalExceptionHandler가 400(COMMON_400_2)으로 응답한다
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    // POST /auth/login 요청을 이 메서드가 처리
    // 새로 만들어지는 것이 없으므로 상태코드는 기본값 200(OK)을 그대로 쓴다 (@ResponseStatus 없음)
    @PostMapping("/login")
    // @Valid: 이메일, 비밀번호가 비어 있으면 서비스까지 가지 않고 400으로 막는다
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
