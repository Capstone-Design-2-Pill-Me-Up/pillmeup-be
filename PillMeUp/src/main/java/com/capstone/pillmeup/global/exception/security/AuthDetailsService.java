package com.capstone.pillmeup.global.exception.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pillmeup.domain.user.entity.Member;
import com.capstone.pillmeup.domain.user.repository.MemberRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	//  username 파라미터에는 JwtFilter가 넣어준 memberId 문자열이 들어옴
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Long memberId = parseMemberId(username);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.INVALID_TOKEN));

        return new AuthDetails(member); // AuthDetails는 UserDetails 구현체
		
	}
	
	private Long parseMemberId(String username) {
        try {
            return Long.valueOf(username);
        } catch (NumberFormatException e) {
            throw new CoreException(ErrorType.INVALID_TOKEN);
        }
    }
	
}
