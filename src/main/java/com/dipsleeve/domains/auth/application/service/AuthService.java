package com.dipsleeve.domains.auth.application.service;

import com.dipsleeve.domains.auth.application.dto.request.SignupRequest;
import com.dipsleeve.domains.auth.application.dto.response.SignupResponse;
import com.dipsleeve.domains.auth.domain.entity.User;
import com.dipsleeve.domains.auth.exception.UserErrorCode;
import com.dipsleeve.domains.auth.repository.UserRepository;
import com.dipsleeve.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 인증(회원가입, 로그인)의 절차와 규칙을 정하는 곳. 스프링이 빈으로 만들어 관리한다(@Service)
// 세부 실행은 직접 하지 않고 전문 부품에 맡긴다(위임).
// 서비스가 아는 것: 무엇을 어떤 순서로, 어떤 조건에서
@Service
// final 필드를 받는 생성자를 만들어 준다 -> 스프링이 이 생성자로 필요한 빈을 넣어 준다 (생성자 주입)
@RequiredArgsConstructor
public class AuthService {

    // DB와 주고받는 통로, 비밀번호 해시 도구. 스프링이 만들어 둔 빈이 들어온다
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Transactional: 이 메서드 전체를 하나의 작업 묶음으로 처리한다.
    // 중간에 예외가 나면 DB에 한 변경이 모두 취소(롤백)된다
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 1. 중복 확인: 있으면 에러 항목을 담아 던진다. 던지는 순간 이 메서드는 멈추고 아래는 실행되지 않는다
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(UserErrorCode.EMAIL_DUPLICATED);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATED);
        }

        // 2. 엔티티 만들기: 비밀번호는 해시한 값만 저장한다 (원본은 어디에도 저장하지 않음)
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        // 3. 저장하고, 저장된 엔티티(id가 채워짐)를 응답 객체로 바꿔 돌려준다
        return SignupResponse.from(userRepository.save(user));
    }
}
