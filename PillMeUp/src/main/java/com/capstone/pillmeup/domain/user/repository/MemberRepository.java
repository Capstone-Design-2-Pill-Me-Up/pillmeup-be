package com.capstone.pillmeup.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.user.entity.Member;
import com.capstone.pillmeup.domain.user.entity.Provider;

public interface MemberRepository extends JpaRepository<Member, Long> {
	
	// 소셜/로컬 공통: provider + providerId 로 조회
    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

    // 로컬 로그인용: email + provider=LOCAL 로 조회
    Optional<Member> findByEmailAndProvider(String email, Provider provider);

    // 회원가입 중복 체크용
    boolean existsByEmailAndProvider(String email, Provider provider);
    
}
