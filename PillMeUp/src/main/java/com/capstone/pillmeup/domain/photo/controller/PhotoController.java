package com.capstone.pillmeup.domain.photo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.capstone.pillmeup.domain.photo.dto.response.PhotoUploadResponse;
import com.capstone.pillmeup.domain.photo.service.PhotoService;
import com.capstone.pillmeup.global.exception.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/photo")
@RequiredArgsConstructor
@Tag(name = "Photo", description = "알약 사진 업로드 및 분석 API")
public class PhotoController {

	private final PhotoService photoService;
	
	@Operation(
        summary = "알약 사진 업로드 + AI 분석",
        description = """
            사용자가 업로드한 사진을 분석하여 여러 알약을 감지하고 item_seq 리스트를 반환합니다.

            ### 저장 구조
            - 사진 1장 업로드 → member_history 1개 생성
            - 감지된 알약이 N개 → member_photo N개 생성 (알약당 1 row)
            - member_history는 대표 itemSeq를 저장하지 않음
              → 실제 감지된 모든 값은 member_photo에 저장됨

            ### 조회 방식
            - 히스토리 목록 조회 → 사용자가 어떤 사진을 언제 조회했는지 표시
            - 히스토리 상세 조회 → 연결된 모든 member_photo(itemSeq 정보 포함) 조회 가능
            """
    )
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ApiResponse<PhotoUploadResponse> uploadPhoto(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam(name = "memberId") Long memberId,

            @Parameter(description = "업로드할 알약 이미지 파일", required = true)
            @RequestPart(name = "file") MultipartFile file) {

        PhotoUploadResponse response = photoService.uploadAndAnalyze(memberId, file);
        return ApiResponse.success(response);
    }
	
}
