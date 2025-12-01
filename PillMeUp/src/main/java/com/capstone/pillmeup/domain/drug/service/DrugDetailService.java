package com.capstone.pillmeup.domain.drug.service;

import java.util.List;
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
import com.capstone.pillmeup.domain.history.entity.MemberHistory;
import com.capstone.pillmeup.domain.history.repository.MemberHistoryRepository;
import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;
import com.capstone.pillmeup.domain.photo.repository.MemberPhotoRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrugDetailService {

	private final DrugRepository drugRepository;
    private final DrugTypeRepository drugTypeRepository;
    private final MemberPhotoRepository memberPhotoRepository;
    private final MemberHistoryRepository memberHistoryRepository;
    private final ChatGptService chatGptService;
    
    private String cleanText(String text) {
        if (text == null) return null;
        return text
                .replace("?", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
    
    // 특정 의약품 상세 조회
    @Transactional
    public DrugDetailResponse getDrugDetail(String itemSeq, Long historyId) {

        // 1. 의약품 검증
        Drug drug = drugRepository.findByItemSeq(itemSeq)
                .orElseThrow(() -> new CoreException(ErrorType.DRUG_NOT_FOUND, "해당 품목코드의 의약품을 찾을 수 없습니다."));

        boolean updated = false;

        // 2. GPT로 누락 필드 보완
        try {
        	
        	if (isNullOrEmpty(drug.getEfcyQesitm())) {
                drug.setEfcyQesitm(cleanText(
                        chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "효능 효과")
                ));
                updated = true;
            }
            if (isNullOrEmpty(drug.getUseMethodQesitm())) {
                drug.setUseMethodQesitm(cleanText(
                        chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "용법 용량")
                ));
                updated = true;
            }
            if (isNullOrEmpty(drug.getAtpnQesitm())) {
                drug.setAtpnQesitm(cleanText(
                        chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "사용상 주의사항")
                ));
                updated = true;
            }
            if (isNullOrEmpty(drug.getIntrcQesitm())) {
                drug.setIntrcQesitm(cleanText(
                        chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "상호작용")
                ));
                updated = true;
            }
            if (isNullOrEmpty(drug.getSeQesitm())) {
                drug.setSeQesitm(cleanText(
                        chatGptService.generateDetailField(drug.getItemName(), drug.getEntpName(), "부작용")
                ));
                updated = true;
            }
            
        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_DATA_GENERATION_FAILED, "GPT를 통한 의약품 상세정보 생성 중 오류가 발생했습니다.");
        }

        if (updated) drugRepository.save(drug);

        // 3. DUR 주의사항 조회
        List<DrugType> types = drugTypeRepository.findByDrug_ItemSeq(itemSeq);
        if (types.isEmpty()) {
            throw new CoreException(ErrorType.DRUG_TYPE_NOT_FOUND, "해당 의약품의 DUR 주의사항이 존재하지 않습니다.");
        }
        
        types.forEach(type -> {
            type.setDescription(cleanText(type.getDescription()));
        });

        // 4. historyId가 있을 경우 S3 이미지 조회
        String fileUrl = null;
        String gptSummary = null;
        
        if (historyId != null) {
        	
            fileUrl = memberPhotoRepository
            		.findTopByHistoryId_HistoryIdOrderByCreatedAtDesc(historyId)
                    .map(MemberPhoto::getFileUrl)
                    .orElse(null);
            
            gptSummary = memberHistoryRepository.findById(historyId)
                    .map(MemberHistory::getGptCautionSummary)
                    .orElse(null);
            
        }

        // 5. DTO 변환
        return DrugDetailResponse.builder()
                .itemSeq(drug.getItemSeq())
                .itemName(cleanText(drug.getItemName()))
                .entpName(cleanText(drug.getEntpName()))
                .chart(cleanText(drug.getChart()))
                .classNo(cleanText(drug.getClassNo()))
                .materialName(cleanText(drug.getMaterialName()))
                .validTerm(cleanText(drug.getValidTerm()))
                .efcyQesitm(cleanText(drug.getEfcyQesitm()))
                .useMethodQesitm(cleanText(drug.getUseMethodQesitm()))
                .atpnQesitm(cleanText(drug.getAtpnQesitm()))
                .intrcQesitm(cleanText(drug.getIntrcQesitm()))
                .seQesitm(cleanText(drug.getSeQesitm()))
                .fileUrl(fileUrl)
                .gptCautionSummary(gptSummary)

                .cautions(
                        types.stream()
                                .map(type ->
                                        DrugTypeResponse.builder()
                                                .typeCode(type.getTypeCode() != null ? type.getTypeCode().name() : null)
                                                .typeName(cleanText(type.getTypeName()))
                                                .description(cleanText(type.getDescription()))
                                                .level(getWarningLevelByType(type.getTypeCode() != null ? type.getTypeCode().name() : null))
                                                .message(cleanText(type.getDescription()))
                                                .build()
                                )
                                .collect(Collectors.toList())
                )
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
