package com.dipsleeve.domains.auth.application.service;

import com.dipsleeve.domains.auth.application.dto.request.SignupRequest;
import com.dipsleeve.domains.auth.application.dto.response.SignupResponse;
import com.dipsleeve.domains.auth.domain.entity.User;
import com.dipsleeve.domains.auth.exception.UserErrorCode;
import com.dipsleeve.domains.auth.repository.UserRepository;
import com.dipsleeve.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // 이 메서드에는 일부러 @Transactional을 붙이지 않는다.
    // DB에 쓰는 일이 save 한 번뿐이라 묶어서 롤백할 변경이 없고, save는 리포지토리가 알아서 트랜잭션으로 처리한다.
    // 전체를 하나로 묶으면 저장이 실패한 뒤 같은 트랜잭션 안에서 다시 조회할 때 에러가 나서(아래 3번 참고) 409로 바꿀 수 없다.
    // 나중에 DB에 쓰는 일이 여러 개로 늘어나면 그때 다시 붙인다
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
        //    1번에서 확인한 뒤 저장하기 전에 같은 값으로 다른 요청이 먼저 저장되면(동시 가입),
        //    DB의 UNIQUE 제약이 막고 DataIntegrityViolationException이 발생한다.
        //    그대로 두면 500이 되므로, 어느 값이 겹쳤는지 다시 확인해 409로 응답한다
        try {
            return SignupResponse.from(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(UserErrorCode.EMAIL_DUPLICATED);
            }
            if (userRepository.existsByNickname(request.nickname())) {
                throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATED);
            }
            // 이메일도 닉네임도 아닌 다른 이유의 제약 위반은 예상 못 한 오류이므로 그대로 던져 500으로 처리한다
            throw e;
        }
    }
}
