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
    private String fileName;
    private String fileUrl;
    private String detectedName;
    private Double confidence;
    private String createdAt;
	
}
