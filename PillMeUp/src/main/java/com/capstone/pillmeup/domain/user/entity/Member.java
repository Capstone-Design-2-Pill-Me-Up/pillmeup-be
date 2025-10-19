package com.capstone.pillmeup.domain.user.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "member",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_member_provider",
        columnNames = {"provider", "provider_id"}
    ),
    indexes = {
        @Index(name = "idx_member_email_provider", columnList = "email, provider")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    // VARCHAR(20) NOT NULL (LOCAL / KAKAO / NAVER)
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private Provider provider;

    // VARCHAR(200) NOT NULL
    @Column(name = "provider_id", nullable = false, length = 200)
    private String providerId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "email", length = 255)
    private String email;

    // LOCAL 전용, SOCIAL은 NULL 가능. 응답 직렬화 시 숨김
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", length = 255)
    private String password;

    // VARCHAR(20) NOT NULL DEFAULT 'SOCIAL'  (LOCAL / SOCIAL)
    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false, length = 20)
    @Builder.Default
    private MemberType memberType = MemberType.SOCIAL;

    // TINYINT(1) NOT NULL DEFAULT 1
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // TINYINT(1) NOT NULL DEFAULT 0
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    // TIMESTAMP DEFAULT CURRENT_TIMESTAMP (DB 기본값 사용)
    @Column(
        name = "created_at",
        insertable = false, updatable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private LocalDateTime createdAt;

    // TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP (DB 자동 갱신 사용)
    @Column(
        name = "updated_at",
        insertable = false, updatable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
    )
    private LocalDateTime updatedAt;

    
    /* ===== 편의/도메인 메서드 ===== */

    // LOCAL 회원 생성 (email을 providerId로 사용 추천)
    public static Member local(String email, String encodedPassword, String name) {
        return Member.builder()
                .provider(Provider.LOCAL)
                .providerId(email)
                .email(email)
                .password(encodedPassword)
                .name(name)
                .memberType(MemberType.LOCAL)
                .build();
    }

    // SOCIAL 회원 생성
    public static Member social(Provider provider, String providerId, String name) {
        return Member.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .memberType(MemberType.SOCIAL)
                .build();
    }

    public void deactivate() { this.isActive = false; }
    public void markDeleted() { this.isDeleted = true; }
    
    // 이름 변경
    public void changeName(String newName) {
        this.name = newName;
    }

    // 비밀번호 변경 (LOCAL 전용)
    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    
}