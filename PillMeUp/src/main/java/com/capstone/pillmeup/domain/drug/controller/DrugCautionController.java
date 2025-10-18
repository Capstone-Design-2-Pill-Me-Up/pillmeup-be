package com.capstone.pillmeup.domain.drug.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pillmeup.domain.drug.dto.request.DrugRequest;
import com.capstone.pillmeup.domain.drug.dto.response.DrugCautionResult;
import com.capstone.pillmeup.domain.drug.service.DrugCautionService;
import com.capstone.pillmeup.global.exception.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/drug")
@RequiredArgsConstructor
@Tag(name = "Drug Caution", description = "AI 분석 결과 기반 의약품 주의사항 조회 API")
public class DrugCautionController {

	private final DrugCautionService drugCautionService;

    /**
     * Vision AI 분석 결과를 기반으로 한 약품 주의사항 조회
     * item_seq 리스트를 입력받아 GPT 주의사항, DUR 주의사항, 사진 정보를 통합 반환
     */
    @Operation(
        summary = "AI 분석 기반 약품 주의사항 조회",
        description = """
            Vision AI 분석 결과로부터 반환된 item_seq 리스트를 입력받아 
            각 의약품의 DUR 주의사항, GPT 요약 주의사항, 사진 정보를 통합 조회합니다.
            """
    )
    @PostMapping("/caution")
    public ApiResponse<DrugCautionResult> getDrugCautions(
    		@Parameter(description = "AI 모델이 반환한 의약품 item_seq 리스트", example = "[\"202002850\", \"195700013\"]")
            @RequestBody DrugRequest request,

            @Parameter(description = "로그인한 회원의 ID (선택)", example = "1")
            @RequestParam(name = "memberId", required = false) Long memberId,

            @Parameter(description = "촬영된 사진 ID (선택)", example = "10")
            @RequestParam(name = "photoId", required = false) Long photoId
    ) {
        return ApiResponse.success(drugCautionService.getDrugCautions(request, memberId, photoId));
    }
	
}
