package com.capstone.pillmeup.domain.photo.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoUploadResponse {

	private String fileUrl;
    private String detectedName; // AI 탐지 결과 이름 (AI_MODEL_PENDING 등)
    private String confidence;   // 신뢰도 (AI 모델 도입 후 값 변경 예정)
    private String message;
    private List<String> itemSeqList;
	
}
