package com.capstone.pillmeup.global.exception.security;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.capstone.pillmeup.domain.user.entity.Member;

import lombok.Getter;

@Getter
public class AuthDetails implements UserDetails {

	@Serial
    private static final long serialVersionUID = 1L;

    // 엔티티는 직렬화 제외(지연로딩/순환참조 예방)
    private final transient Member member;

    // 직렬화 안전한 스냅샷 필드들
    private final Long memberId;
    private final String email;
    private final String password; // SOCIAL은 null 가능
    private final boolean active;
    private final boolean deleted;

    public AuthDetails(Member member) {
        this.member   = member;
        this.memberId = member.getMemberId();
        this.email    = member.getEmail();
        this.password = member.getPassword();
        this.active   = member.isActive();
        this.deleted  = member.isDeleted();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역할 고정: USER
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override public String getPassword() { return password; }

    // username은 토큰 sub와 동일하게 memberId 문자열 사용
    @Override public String getUsername() { return String.valueOf(memberId); }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // 활성/삭제 상태 반영
    @Override public boolean isEnabled() { return active && !deleted; }
	
}
