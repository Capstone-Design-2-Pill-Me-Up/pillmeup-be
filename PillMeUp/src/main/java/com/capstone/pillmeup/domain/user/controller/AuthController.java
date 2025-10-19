package com.capstone.pillmeup.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pillmeup.domain.user.dto.request.SignInRequest;
import com.capstone.pillmeup.domain.user.dto.request.SignUpRequest;
import com.capstone.pillmeup.domain.user.dto.response.MemberResponse;
import com.capstone.pillmeup.domain.user.service.AuthService;
import com.capstone.pillmeup.global.exception.response.ApiResponse;
import com.capstone.pillmeup.global.exception.security.jwt.dto.TokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입/로그인/로그아웃 API")
public class AuthController {

	private final AuthService authService;
	
	@Operation(summary = "회원가입", description = "로컬 계정 회원가입을 수행합니다.")
    @PostMapping("/sign-up")
    public ApiResponse<MemberResponse> signup(@RequestBody SignUpRequest req) {
        return ApiResponse.success(authService.signup(req));
    }

    @Operation(summary = "로그인", description = "로컬 계정 로그인을 수행하고 JWT 토큰을 발급합니다.")
    @PostMapping("/sign-in")
    public ApiResponse<TokenResponse> signin(@RequestBody SignInRequest req) {
        return ApiResponse.success(authService.signin(req));
    }
    
    @Operation(
    	    summary = "로그아웃",
    	    description = """
    	        JWT(Local) / OAuth(소셜) 로그아웃을 수행합니다.<br><br>

    	        <b>요청 헤더</b><br>
    	        - Authorization: <code>Bearer {access_token}</code><br>
    	          (Local은 서버에서 발급한 JWT, Kakao/Naver는 해당 플랫폼의 Access Token 사용)<br><br>

    	        <b>요청 파라미터</b><br>
    	        - provider: <code>"LOCAL" | "KAKAO" | "NAVER"</code><br>
    	          (프론트엔드에서 로그인 시점에 저장해둔 provider 값을 전달해야 합니다.)
    	        """
    	)
    @PostMapping("/logout")
    public ApiResponse<String> logout(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "provider", required = false) String provider
    ) {
        authService.logout(token, provider);
        return ApiResponse.success("로그아웃이 완료되었습니다.");
    }

}
