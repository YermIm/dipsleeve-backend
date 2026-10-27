package com.dipsleeve.domains.auth.repository;

import com.dipsleeve.domains.auth.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// users 테이블을 읽고 쓰는 통로.
// 구현 클래스를 우리가 만들지 않는다. 스프링 데이터 JPA가 앱이 시작될 때 알아서 만들어 준다.
// JpaRepository<User, Long>: "User 엔티티를 다루고, 그 PK 타입은 Long이다"라는 뜻
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 한 명 조회 (로그인에서 사용).
    // Optional: 없을 수도 있다는 것을 타입으로 표현한다. 없으면 빈 Optional을 돌려준다 (null 대신)
    Optional<User> findByEmail(String email);

    // 이 이메일을 쓰는 사용자가 있는지 확인 (회원가입 중복 검사에서 사용). 있으면 true
    boolean existsByEmail(String email);

    // 이 닉네임을 쓰는 사용자가 있는지 확인 (회원가입 중복 검사에서 사용)
    boolean existsByNickname(String nickname);
}
