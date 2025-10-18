package com.capstone.pillmeup.domain.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberPhotoDto {

	private Long photoId;
    private Long historyId;
    private Long memberId;
    private String itemSeq;
    private String fileName;      // 파일명 (UUID)
    private String fileUrl;       // 파일 접근 경로 (S3 URL) // TODO: S3 연동 후 실제 URL 삽입
    private String detectedName;
    private Double confidence;
    private String createdAt;
	
}
