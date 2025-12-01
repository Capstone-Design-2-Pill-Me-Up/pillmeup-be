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
import com.capstone.pillmeup.domain.drug.dto.response.DrugHistoryDetailService;
import com.capstone.pillmeup.domain.drug.service.DrugCautionService;
import com.capstone.pillmeup.domain.history.dto.response.HistoryDetailResponse;
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
	private final DrugHistoryDetailService drugHistoryDetailService;

	@Operation(
	    summary = "AI 분석 결과 기반 약품 주의사항 처리",
	    description = """
	        하나의 분석(historyId) 안에 포함된 여러 사진/알약을 기반으로
	        DUR 보완 + GPT 종합 분석을 생성합니다.
	        
	        - historyId는 사진 업로드 시 생성됩니다.
	        - 여러 사진이 하나의 historyId에 묶입니다.
	        """
	)
	@PostMapping("/caution")
	public ApiResponse<DrugCautionResult> getDrugCautions(
		@Parameter(
	            description = "AI 모델이 반환한 item_seq 리스트",
	            example = "{\"itemSeqList\": [\"195700013\", \"200600026\"]}"
        )
        @RequestBody DrugRequest request,

        @Parameter(
            name = "memberId",
            description = "요청을 보낸 로그인 사용자 ID",
            example = "1"
        )
        @RequestParam(name = "memberId") Long memberId,

        @Parameter(
            name = "historyId",
            description = "사진 업로드 시 생성된 분석 이력 ID",
            example = "1"
        )
        @RequestParam(name = "historyId") Long historyId
	) {
	    return ApiResponse.success(
	            drugCautionService.getDrugCautions(request, memberId, historyId)
	    );
	}


	@Operation(
	    summary = "특정 분석 이력 상세 조회",
	    description = """
	        historyId를 기준으로 사용자가 수행한 하나의 분석 결과를 조회합니다.

	        반환 내용:
	        - 해당 분석에서 감지된 모든 알약 정보(item_seq 기준)
	        - 각 알약의 기본 정보 및 DUR 주의사항 목록
	        - 분석 당시 생성된 GPT 종합 주의사항 요약(gpt_caution_summary)
	        - 분석에 사용된 모든 이미지 URL 목록

	        사용 시나리오:
	        - 최근 분석 기록 목록에서 '상세보기' 클릭 시 사용되는 API
	        - 하나의 historyId 안에는 여러 사진 + 여러 알약이 포함될 수 있음

	        주의:
	        - historyId는 member_history 테이블의 고유 PK이며,
	          분석 단위로 구성됨(사진/알약 개수와 무관)
	        """
	)
	@GetMapping("/history/{historyId}")
	public ApiResponse<HistoryDetailResponse> getHistoryDetail(
		@Parameter(
	            name = "historyId",
	            description = "조회할 분석 이력의 ID (member_history.history_id)",
	            example = "1",
	            required = true
	        )
        @PathVariable(name = "historyId") Long historyId
	) {
	    return ApiResponse.success(drugHistoryDetailService.getHistoryDetail(historyId));
	}

}
