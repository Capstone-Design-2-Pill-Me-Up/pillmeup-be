package com.capstone.pillmeup.domain.drug.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.pillmeup.domain.drug.dto.request.DrugRequest;
import com.capstone.pillmeup.domain.drug.dto.response.DrugCautionResult;
import com.capstone.pillmeup.domain.drug.dto.response.DrugDetailResponse;
import com.capstone.pillmeup.domain.drug.service.DrugCautionService;
import com.capstone.pillmeup.domain.drug.service.DrugDetailService;
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
	private final DrugDetailService drugDetailService;

	@Operation(
	        summary = "AI 분석 기반 약품 주의사항 조회",
	        description = "AI가 반환한 item_seq 리스트를 기반으로 DUR 주의사항과 GPT 요약 결과를 조회합니다. "
	                    + "historyId가 없는 경우에도 photoId만으로 조회가 가능합니다."
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

	    @Operation(
	        summary = "특정 의약품 상세정보 조회",
	        description = "품목코드(item_seq)를 기반으로 의약품 상세정보와 DUR 주의사항을 조회합니다. "
	                    + "historyId가 존재하는 경우 해당 이력에 저장된 사진 URL을 함께 반환합니다."
	    )
	    @GetMapping("/{itemSeq}")
	    public ApiResponse<DrugDetailResponse> getDrugDetail(
	            @Parameter(description = "조회할 의약품의 품목코드", example = "195700013")
	            @PathVariable("itemSeq") String itemSeq,

	            @Parameter(description = "조회할 이력의 ID (선택)", example = "1")
	            @RequestParam(name = "historyId", required = false) Long historyId
	    ) {
	        return ApiResponse.success(drugDetailService.getDrugDetail(itemSeq, historyId));
	    }
	
}
