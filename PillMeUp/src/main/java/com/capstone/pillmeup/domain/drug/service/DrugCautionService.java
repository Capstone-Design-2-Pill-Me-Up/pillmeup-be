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
import com.capstone.pillmeup.domain.user.entity.Member;
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

    @Transactional
    public DrugCautionResult getDrugCautions(DrugRequest request, Long memberId, Long photoId) {

        // 1. 요청값 검증
        if (request == null || request.getItemSeqList() == null || request.getItemSeqList().isEmpty()) {
            throw new CoreException(ErrorType.VALIDATION_ERROR, "itemSeqList가 전달되지 않았습니다.");
        }

        // 2. 중복 제거
        List<String> dedupItemSeqs = request.getItemSeqList().stream().distinct().toList();

        // 3. 약품 기본정보 조회
        List<Drug> drugs = drugRepository.findByItemSeqIn(dedupItemSeqs);
        if (drugs.isEmpty()) {
            throw new CoreException(ErrorType.DRUG_NOT_FOUND, "요청한 의약품이 존재하지 않습니다.");
        }

        // 4. DUR 주의사항 조회
        List<DrugType> types = drugTypeRepository.findByDrug_ItemSeqIn(dedupItemSeqs);
        if (types.isEmpty()) {
            throw new CoreException(ErrorType.DRUG_TYPE_NOT_FOUND);
        }

        // 5. Photo 조회 → DTO 매핑
        Map<String, MemberPhotoDto> photoByItemSeq = new HashMap<>();
        MemberHistory history = null;

        if (memberId != null && photoId != null) {

            MemberPhoto photo = memberPhotoRepository.findById(photoId)
                    .orElseThrow(() -> new CoreException(ErrorType.PHOTO_NOT_FOUND));

            // history 존재하면 그대로 사용
            if (photo.getHistoryId() != null) {
                history = photo.getHistoryId();
            }
            // 없으면 새로 생성
            else {
                history = MemberHistory.builder()
                        .memberId(Member.builder().memberId(memberId).build())
                        .gptCautionSummary(null)  // 나중에 아래에서 저장됨
                        .build();

                memberHistoryRepository.save(history);

                // 사진과 연결
                photo.linkHistory(history);
                memberPhotoRepository.save(photo);
            }

            // DTO 매핑
            photoByItemSeq.put(
                    photo.getItemSeq().getItemSeq(),
                    MemberPhotoDto.builder()
                            .photoId(photo.getPhotoId())
                            .historyId(history.getHistoryId())
                            .memberId(photo.getMemberId().getMemberId())
                            .itemSeq(photo.getItemSeq().getItemSeq())
                            .fileName(photo.getFileName())
                            .fileUrl(photo.getFileUrl())
                            .detectedName(photo.getDetectedName())
                            .confidence(photo.getConfidence())
                            .createdAt(photo.getCreatedAt().toString())
                            .build()
            );
        }

        // 6. GPT 개별 DUR 설명 누락시 보완
        for (DrugType type : types) {
            if (type.getDescription() == null || type.getDescription().isBlank()) {
            	
                try {
                    String generated = chatGptService.generateDrugTypeDescription(
                            type.getDrug().getItemName(),
                            type.getTypeCode().name(),
                            type.getTypeName()
                    );
                    type.setDescription(generated);
                    drugTypeRepository.save(type);

                } catch (Exception e) {
                    throw new CoreException(ErrorType.DRUG_CAUTION_GENERATION_FAILED,
                            "GPT 요청 실패 (" + type.getDrug().getItemSeq() + ")");
                }
                
            }
        }

        // 7. GPT 종합 요약 생성
        String overallSummary;
        try {
            List<String> itemNames = drugs.stream().map(Drug::getItemName).toList();
            List<String> cautionNames = types.stream().map(DrugType::getTypeName).distinct().toList();

            overallSummary = chatGptService.generateOverallCaution(itemNames, cautionNames);

        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_SUMMARY_FAILED);
        }

        // 8. 생성된 요약을 MemberHistory에 저장
        if (history != null) {
            history.updateSummary(overallSummary);
        }


        // 9. 응답 변환
        Map<String, List<DrugType>> typesBySeq = types.stream()
                .collect(Collectors.groupingBy(dt -> dt.getDrug().getItemSeq()));

        List<DrugCautionResponse> responses = drugs.stream()
                .map(drug -> DrugCautionResponse.of(
                        drug,
                        typesBySeq.getOrDefault(drug.getItemSeq(), Collections.emptyList()),
                        overallSummary,
                        photoByItemSeq.get(drug.getItemSeq())
                ))
                .toList();

        return DrugCautionResult.builder()
                .foundDrugs(responses)
                .missingItems(
                        dedupItemSeqs.stream()
                                .filter(seq -> drugs.stream().noneMatch(d -> d.getItemSeq().equals(seq)))
                                .toList()
                )
                .historyId(history != null ? history.getHistoryId() : null)
                .build();
        
    }
    	
}
