package com.capstone.pillmeup.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pillmeup.domain.user.dto.request.MemberPasswordRequest;
import com.capstone.pillmeup.domain.user.dto.request.MemberProfileRequest;
import com.capstone.pillmeup.domain.user.dto.response.MemberProfileResponse;
import com.capstone.pillmeup.domain.user.entity.Provider;
import com.capstone.pillmeup.domain.user.service.MemberProfileService;
import com.capstone.pillmeup.global.exception.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "회원 프로필 조회/수정/탈퇴")
public class MemberProfileController {

	private final MemberProfileService memberProfileService;

    @Operation(summary = "회원 프로필 조회", description = "JWT 토큰을 기반으로 로그인한 사용자의 프로필 정보를 반환합니다.")
    @GetMapping("/profile")
    public ApiResponse<MemberProfileResponse> getProfile(
            @AuthenticationPrincipal(expression = "provider") Provider provider,
            @AuthenticationPrincipal(expression = "providerId") String providerId
    ) {
        MemberProfileResponse response = memberProfileService.getProfile(provider, providerId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "회원 프로필 수정", description = "이름만 수정 가능합니다. 이메일은 변경할 수 없습니다.")
    @PutMapping("/profile")
    public ApiResponse<String> updateProfile(
            @AuthenticationPrincipal(expression = "provider") Provider provider,
            @AuthenticationPrincipal(expression = "providerId") String providerId,
            @RequestBody MemberProfileRequest request
    ) {
        memberProfileService.updateProfile(provider, providerId, request);
        return ApiResponse.success("회원 이름이 수정되었습니다.");
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다. (LOCAL 전용)")
    @PutMapping("/password")
    public ApiResponse<String> changePassword(
            @AuthenticationPrincipal(expression = "provider") Provider provider,
            @AuthenticationPrincipal(expression = "providerId") String providerId,
            @RequestBody MemberPasswordRequest request
    ) {
        memberProfileService.changePassword(provider, providerId, request);
        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "회원 계정을 삭제(또는 비활성화)합니다.")
    @DeleteMapping
    public ApiResponse<String> deleteAccount(
            @AuthenticationPrincipal(expression = "provider") Provider provider,
            @AuthenticationPrincipal(expression = "providerId") String providerId
    ) {
        memberProfileService.deleteAccount(provider, providerId);
        return ApiResponse.success("회원 탈퇴가 완료되었습니다.");
    }
	
}
