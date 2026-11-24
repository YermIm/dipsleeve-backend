package com.dipsleeve.domains.auth.application.service;

import com.dipsleeve.domains.auth.application.dto.request.LoginRequest;
import com.dipsleeve.domains.auth.application.dto.request.SignupRequest;
import com.dipsleeve.domains.auth.application.dto.response.LoginResponse;
import com.dipsleeve.domains.auth.application.dto.response.SignupResponse;
import com.dipsleeve.domains.auth.domain.entity.User;
import com.dipsleeve.domains.auth.exception.AuthErrorCode;
import com.dipsleeve.domains.auth.exception.UserErrorCode;
import com.dipsleeve.domains.auth.repository.UserRepository;
import com.dipsleeve.global.exception.BusinessException;
import com.dipsleeve.global.security.jwt.JwtProvider;
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

    // 존재하지 않는 이메일로 로그인할 때 비교하는 척하기 위한 가짜 해시(BCrypt, 비용 10). 실제 사용자의 비밀번호가 아니다.
    // PasswordEncoder의 비용 설정을 바꾸면 이 값도 같은 비용으로 다시 만들어야 응답 시간이 맞는다
    private static final String DUMMY_PASSWORD_HASH = "$2a$10$CqmuVVs.USnr5IDZegrzm.FnCGqWtYkMwXxrCbDlF8jgt6xgGUE/m";

    // DB와 주고받는 통로, 비밀번호 해시 도구, 토큰 발급 도구. 스프링이 만들어 둔 빈이 들어온다
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

    // 로그인: 이메일과 비밀번호가 맞으면 Access 토큰을 발급한다.
    // DB에 쓰는 일이 없고 조회만 하므로 @Transactional을 붙이지 않는다
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자를 찾는다. 없으면 null
        User user = userRepository.findByEmail(request.email()).orElse(null);

        // 2. 비교할 해시를 정한다. 사용자가 없거나 저장된 비밀번호가 없으면(나중에 소셜 로그인으로 가입한 경우) 가짜 해시로 대신한다.
        //    그래야 어떤 경우에도 matches가 한 번씩 실행되어 응답 시간이 비슷해진다
        //    (사용자가 없을 때만 비교를 건너뛰면 응답이 눈에 띄게 빨라져서, 상태코드가 같아도 응답 시간으로 가입 여부를 알아낼 수 있다)
        boolean hasPassword = user != null && user.getPassword() != null;
        String storedHash = hasPassword ? user.getPassword() : DUMMY_PASSWORD_HASH;

        // 3. 비밀번호를 비교한다. 해시는 되돌릴 수 없어서, 입력한 원본을 같은 방식으로 해시해 저장된 값과 맞는지 확인한다(matches)
        boolean passwordMatches = passwordEncoder.matches(request.password(), storedHash);

        // 4. 사용자가 없을 때와 비밀번호가 틀릴 때 같은 에러를 던진다 (둘을 구분해서 알려 주지 않기 위해)
        if (!hasPassword || !passwordMatches) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 5. 토큰을 발급해서 응답 객체에 담는다. 유효 시간도 JwtProvider에서 가져와 토큰의 실제 만료와 맞춘다
        String accessToken = jwtProvider.createAccessToken(user.getId());
        return LoginResponse.of(accessToken, jwtProvider.getAccessTokenExpirationSeconds());
    }
}
