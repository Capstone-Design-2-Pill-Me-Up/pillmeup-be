package com.capstone.pillmeup.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pillmeup.domain.user.dto.request.SignInRequest;
import com.capstone.pillmeup.domain.user.dto.request.SignUpRequest;
import com.capstone.pillmeup.domain.user.dto.response.MemberResponse;
import com.capstone.pillmeup.domain.user.service.AuthService;
import com.capstone.pillmeup.global.exception.response.ApiResponse;
import com.capstone.pillmeup.global.exception.security.jwt.dto.TokenResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	/** 회원가입 (LOCAL) */
    @PostMapping("/sign-up")
    public ApiResponse<MemberResponse> signup(@RequestBody SignUpRequest req) {
        return ApiResponse.success(authService.signup(req));
    }

    /** 로그인 (LOCAL) */
    @PostMapping("/sign-in")
    public ApiResponse<TokenResponse> signin(@RequestBody SignInRequest req) {
        return ApiResponse.success(authService.signin(req));
    }
	
}
