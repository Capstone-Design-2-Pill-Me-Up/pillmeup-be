package com.capstone.pillmeup.domain.drug.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.capstone.pillmeup.domain.drug.entity.Drug;
import com.capstone.pillmeup.domain.drug.entity.DrugType;
import com.capstone.pillmeup.domain.photo.dto.MemberPhotoDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrugCautionResponse {

	private String itemSeq;
    private String itemName;
    private String entpName;
    private String overallCaution;

    private MemberPhotoDto photo;           // 촬영 이미지 정보 (S3 연동 전 null)
    private List<DrugTypeInfo> warnings;    // DUR 주의사항 목록

    public static DrugCautionResponse of(
            Drug drug,
            List<DrugType> types,
            String overallCaution,
            MemberPhotoDto photo
    ) {
        return DrugCautionResponse.builder()
                .itemSeq(drug.getItemSeq())
                .itemName(drug.getItemName())
                .entpName(drug.getEntpName())
                .overallCaution(overallCaution)
                .photo(photo)  // ✅ 사진 정보 추가
                .warnings(types.stream()
                        .map(t -> DrugTypeInfo.builder()
                                .typeCode(t.getTypeCode() != null ? t.getTypeCode().name() : null)
                                .typeName(t.getTypeName())
                                .description(t.getDescription())
                                .level(getWarningLevelByType(t.getTypeCode() != null ? t.getTypeCode().name() : null))
                                .message(t.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    // 주의사항 위험도(Level)을 DUR 코드 기준으로 분류 (프론트 구분용)
    private static String getWarningLevelByType(String typeCode) {
        if (typeCode == null) return "low";
        return switch (typeCode) {
            case "A", "B", "C" -> "high";
            case "D", "E", "F" -> "medium";
            default -> "low";
        };
    }
	
}
