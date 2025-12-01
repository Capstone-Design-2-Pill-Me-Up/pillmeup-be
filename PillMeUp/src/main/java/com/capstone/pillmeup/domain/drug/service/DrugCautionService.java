package com.capstone.pillmeup.domain.drug.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pillmeup.domain.ai.service.ChatGptService;
import com.capstone.pillmeup.domain.drug.dto.request.DrugRequest;
import com.capstone.pillmeup.domain.drug.dto.response.DrugCautionResponse;
import com.capstone.pillmeup.domain.drug.dto.response.DrugCautionResult;
import com.capstone.pillmeup.domain.drug.entity.Drug;
import com.capstone.pillmeup.domain.drug.entity.DrugType;
import com.capstone.pillmeup.domain.drug.repository.DrugRepository;
import com.capstone.pillmeup.domain.drug.repository.DrugTypeRepository;
import com.capstone.pillmeup.domain.history.entity.MemberHistory;
import com.capstone.pillmeup.domain.history.repository.MemberHistoryRepository;
import com.capstone.pillmeup.domain.photo.dto.MemberPhotoDto;
import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;
import com.capstone.pillmeup.domain.photo.repository.MemberPhotoRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrugCautionService {

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
    
    @Transactional
    public DrugCautionResult getDrugCautions(DrugRequest request, Long memberId, Long historyId) {

        // 1. 요청값 검증
        if (request == null || request.getItemSeqList() == null || request.getItemSeqList().isEmpty()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "itemSeqList가 전달되지 않았습니다.");
        }

        // 2. history 조회
        MemberHistory history = memberHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CoreException(ErrorType.HISTORY_NOT_FOUND));

        // 3. 해당 history의 모든 사진 조회
        List<MemberPhoto> photos = memberPhotoRepository.findByHistoryId_HistoryId(historyId);

        // 4. 사진을 itemSeq 기준으로 매핑
        Map<String, MemberPhotoDto> photoByItemSeq = new HashMap<>();
        for (MemberPhoto p : photos) {
            photoByItemSeq.put(
                    p.getItemSeq().getItemSeq(),
                    MemberPhotoDto.builder()
                            .photoId(p.getPhotoId())
                            .historyId(historyId)
                            .memberId(p.getMemberId().getMemberId())
                            .itemSeq(p.getItemSeq().getItemSeq())
                            .fileName(p.getFileName())
                            .fileUrl(p.getFileUrl())
                            .detectedName(p.getDetectedName())
                            .confidence(p.getConfidence())
                            .createdAt(p.getCreatedAt().toString())
                            .build()
            );
        }

        // 5. 중복 제거된 itemSeq
        List<String> dedupItemSeqs = request.getItemSeqList().stream().distinct().toList();

        // 6. 약품 정보 조회
        List<Drug> drugs = drugRepository.findByItemSeqIn(dedupItemSeqs);
        if (drugs.isEmpty()) throw new CoreException(ErrorType.DRUG_NOT_FOUND);

        // 7. DUR 정보 조회
        List<DrugType> types = drugTypeRepository.findByDrug_ItemSeqIn(dedupItemSeqs);

        // 8. 누락된 DUR 설명 GPT로 보완
        for (DrugType type : types) {
            if (type.getDescription() == null || type.getDescription().isBlank()) {
                try {
                    String generated = chatGptService.generateDrugTypeDescription(
                            type.getDrug().getItemName(),
                            type.getTypeCode().name(),
                            type.getTypeName()
                    );
                    type.setDescription(cleanText(generated));
                    drugTypeRepository.save(type);

                } catch (Exception e) {
                    throw new CoreException(ErrorType.DRUG_CAUTION_GENERATION_FAILED);
                }
            }
        }

        // 9. GPT 종합 요약 생성
        List<String> itemNames = drugs.stream().map(Drug::getItemName).distinct().toList();
        List<String> typeNames = types.stream().map(DrugType::getTypeName).distinct().toList();

        String overallSummaryTemp;
        try {
            overallSummaryTemp = chatGptService.generateOverallCaution(itemNames, typeNames);
        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_SUMMARY_FAILED);
        }

        String overallSummary = cleanText(overallSummaryTemp);

        // 10. summary 저장
        history.updateSummary(overallSummary);
        memberHistoryRepository.save(history);

        // 11. 응답 변환
        Map<String, List<DrugType>> typesBySeq = types.stream()
                .collect(Collectors.groupingBy(t -> t.getDrug().getItemSeq()));

        List<DrugCautionResponse> responses = drugs.stream()
                .map(drug -> {
                    drug.setEfcyQesitm(cleanText(drug.getEfcyQesitm()));
                    drug.setUseMethodQesitm(cleanText(drug.getUseMethodQesitm()));
                    drug.setAtpnQesitm(cleanText(drug.getAtpnQesitm()));
                    drug.setIntrcQesitm(cleanText(drug.getIntrcQesitm()));
                    drug.setSeQesitm(cleanText(drug.getSeQesitm()));

                    return DrugCautionResponse.of(
                            drug,
                            typesBySeq.getOrDefault(drug.getItemSeq(), Collections.emptyList()),
                            overallSummary,
                            photoByItemSeq.get(drug.getItemSeq())
                    );
                })
                .toList();

        return DrugCautionResult.builder()
                .foundDrugs(responses)
                .missingItems(
                        dedupItemSeqs.stream()
                                .filter(seq -> drugs.stream().noneMatch(d -> d.getItemSeq().equals(seq)))
                                .toList()
                )
                .historyId(historyId)
                .build();
    }
    	
}
