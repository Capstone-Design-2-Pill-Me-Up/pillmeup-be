package com.capstone.pillmeup.global.exception.security.jwt;

import com.capstone.pillmeup.domain.user.entity.Provider;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal {

	private Long memberId;
    private Provider provider;
    private String providerId;
	
}
