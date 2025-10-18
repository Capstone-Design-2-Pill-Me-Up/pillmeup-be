package com.capstone.pillmeup.domain.drug.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pillmeup.domain.ai.service.ChatGptService;
import com.capstone.pillmeup.domain.drug.dto.response.DrugDetailResponse;
import com.capstone.pillmeup.domain.drug.dto.response.DrugTypeResponse;
import com.capstone.pillmeup.domain.drug.entity.Drug;
import com.capstone.pillmeup.domain.drug.entity.DrugType;
import com.capstone.pillmeup.domain.drug.repository.DrugRepository;
import com.capstone.pillmeup.domain.drug.repository.DrugTypeRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrugDetailService {

	private final DrugRepository drugRepository;
    private final DrugTypeRepository drugTypeRepository;
    private final ChatGptService chatGptService;
    
    // 특정 의약품 상세 조회
    @Transactional
    public DrugDetailResponse getDrugDetail(String itemSeq) {

        // 1. 의약품 존재 여부 검증
        Drug drug = drugRepository.findByItemSeq(itemSeq)
                .orElseThrow(() -> new CoreException(ErrorType.DRUG_NOT_FOUND, "해당 품목코드의 의약품을 찾을 수 없습니다."));

        boolean updated = false; // GPT 결과로 실제 DB 업데이트가 발생했는지 추적

        // 2. GPT를 이용한 누락 필드 자동 보완
        try {
            if (isNullOrEmpty(drug.getEfcyQesitm())) {
                String generated = chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "효능·효과");
                drug.setEfcyQesitm(generated);
                updated = true;
            }

            if (isNullOrEmpty(drug.getUseMethodQesitm())) {
                String generated = chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "용법·용량");
                drug.setUseMethodQesitm(generated);
                updated = true;
            }

            if (isNullOrEmpty(drug.getAtpnQesitm())) {
                String generated = chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "사용상 주의사항");
                drug.setAtpnQesitm(generated);
                updated = true;
            }

            if (isNullOrEmpty(drug.getIntrcQesitm())) {
                String generated = chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "상호작용");
                drug.setIntrcQesitm(generated);
                updated = true;
            }

            if (isNullOrEmpty(drug.getSeQesitm())) {
                String generated = chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "부작용");
                drug.setSeQesitm(generated);
                updated = true;
            }

        } catch (CoreException e) {
            throw e; // GPT 내부 예외 그대로 전달
        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_DATA_GENERATION_FAILED, "GPT를 통한 의약품 상세정보 생성 중 오류가 발생했습니다.");
        }

        // GPT로 생성된 필드가 있다면 DB 업데이트
        if (updated) {
            drugRepository.save(drug);
        }

        // 3. DUR 주의사항 조회
        List<DrugType> types = drugTypeRepository.findByDrug_ItemSeq(itemSeq);
        if (types.isEmpty()) {
            throw new CoreException(ErrorType.DRUG_TYPE_NOT_FOUND, "해당 의약품의 DUR 주의사항이 존재하지 않습니다.");
        }

        // 4. DUR 주의사항 리스트 변환
        List<DrugTypeResponse> cautionList = types.stream()
                .map(type -> DrugTypeResponse.builder()
                        .typeCode(type.getTypeCode() != null ? type.getTypeCode().name() : null)
                        .typeName(type.getTypeName())
                        .description(type.getDescription())
                        .level(getWarningLevelByType(type.getTypeCode() != null ? type.getTypeCode().name() : null))
                        .message(type.getDescription())
                        .build())
                .collect(Collectors.toList());

        // 5. 응답 반환
        return DrugDetailResponse.builder()
                .itemSeq(drug.getItemSeq())
                .itemName(drug.getItemName())
                .entpName(drug.getEntpName())
                .chart(drug.getChart())
                .classNo(drug.getClassNo())
                .materialName(drug.getMaterialName())
                .validTerm(drug.getValidTerm())
                .efcyQesitm(drug.getEfcyQesitm())
                .useMethodQesitm(drug.getUseMethodQesitm())
                .atpnQesitm(drug.getAtpnQesitm())
                .intrcQesitm(drug.getIntrcQesitm())
                .seQesitm(drug.getSeQesitm())
                .cautions(cautionList)
                .build();
        
    }

    // DUR 위험도 레벨 계산
    private static String getWarningLevelByType(String typeCode) {
        if (typeCode == null) return "low";
        return switch (typeCode) {
            case "A", "B", "C" -> "high";   // 병용금기, 임부금기 등
            case "D", "E", "F" -> "medium"; // 용량주의, 특정연령대금기 등
            default -> "low";                // 나머지: 저위험
        };
    }

    // 문자열 null 또는 빈칸 여부 확인
    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
	
}
