package com.capstone.pillmeup.domain.drug.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DrugDetailResponse {

	private String itemSeq;                 // 품목코드
    private String itemName;                // 의약품명
    private String entpName;                // 제조사명
    private String chart;                   // 제형/투여 형태
    private String classNo;                 // 분류번호
    private String materialName;            // 성분정보
    private String validTerm;               // 사용기한

    // 상세 필드
    private String efcyQesitm;              // 효능/효과
    private String useMethodQesitm;         // 용법/용량
    private String atpnQesitm;              // 주의사항
    private String intrcQesitm;             // 상호작용
    private String seQesitm;                // 부작용

    // 주의사항 목록 (DUR)
    private List<DrugTypeResponse> cautions;
	
}
