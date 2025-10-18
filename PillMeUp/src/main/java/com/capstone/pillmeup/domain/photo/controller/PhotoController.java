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
        summary = "알약 사진 업로드 및 분석",
        description = "사용자가 촬영하거나 업로드한 알약 이미지를 S3에 저장하고, AI 분석 준비를 수행합니다."
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
