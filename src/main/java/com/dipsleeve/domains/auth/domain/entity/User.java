package com.dipsleeve.domains.auth.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

// DB의 users 테이블 한 행을 자바 객체로 표현한 클래스
@Entity
// 클래스 이름은 User지만 테이블 이름은 복수형 users로 지정
@Table(name = "users")
// 컨벤션: @Setter 대신 @Getter만 사용 (값은 만들 때 정하고, 바꿀 때는 의미 있는 메서드로)
@Getter
// JPA가 객체를 만들 때 기본 생성자가 필요함. protected로 막아서 밖에서는 new User()를 못 쓰게 함.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // [기본키(PK)] IDENTITY는 DB의 AUTO_INCREMENT에게 번호를 맡긴다
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [로그인 ID] 필수(NOT NULL), 중복 불가(UNIQUE). 길이 기본값이 255라서 따로 지정하지 않음
    @Column(nullable = false, unique = true)
    private String email;

    // [암호화된 비밀번호] 소셜 로그인 대비로 NULL 허용이라 제약을 따로 붙이지 않음
    private String password;

    // [닉네임] 필수, 중복 불가, 최대 50자
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    // [프로필 이미지 주소] NULL 허용, 최대 500자
    @Column(length = 500)
    private String profileImage;

    // [가입 시각] 저장될 때 현재 시각을 자동으로 넣고(@CreationTimestamp), 이후 수정하지 않음
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 객체를 만드는 유일한 통로. id와 createdAt은 DB와 JPA가 채우니 받지 않는다.
    // 사용: User.builder().email("...").nickname("...").build()
    @Builder
    private User(String email, String password, String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
