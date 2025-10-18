package com.capstone.pillmeup.domain.drug.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrugTypeInfo {
	
	private String typeCode;      // DUR 코드 (A~I)
    private String typeName;      // DUR 항목명 (예: 임부금기, 병용금기)
    private String description;   // 주의사항 상세 설명
    private String level;         // 위험도(high, medium, low)
    private String message;       // 요약 메시지 (description 재활용)

}
