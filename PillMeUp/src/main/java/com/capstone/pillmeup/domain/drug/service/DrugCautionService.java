package com.capstone.pillmeup.domain.drug.service;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        // 5. 업로드된 사진 정보 매핑 (historyId가 없는 최초 호출 시 photoId 기준으로 조회)
        Map<String, MemberPhotoDto> photoByItemSeq = new HashMap<>();
        if (memberId != null && photoId != null) {
            MemberPhoto photo = memberPhotoRepository.findById(photoId)
                    .orElseThrow(() -> new CoreException(ErrorType.PHOTO_NOT_FOUND, "해당 사진이 존재하지 않습니다."));

            photoByItemSeq.put(
                photo.getItemSeq().getItemSeq(),
                MemberPhotoDto.builder()
                        .photoId(photo.getPhotoId())
                        .historyId(photo.getHistoryId() != null ? photo.getHistoryId().getHistoryId() : null)
                        .memberId(photo.getMemberId() != null ? photo.getMemberId().getMemberId() : null)
                        .itemSeq(photo.getItemSeq() != null ? photo.getItemSeq().getItemSeq() : null)
                        .fileName(photo.getFileName())
                        .fileUrl(photo.getFileUrl())
                        .detectedName(photo.getDetectedName())
                        .confidence(photo.getConfidence())
                        .createdAt(photo.getCreatedAt() != null ? photo.getCreatedAt().toString() : null)
                        .build()
            );
        }

        // 6. GPT 개별 DUR 주의사항 보완
        for (DrugType type : types) {
            if (type.getDescription() == null || type.getDescription().isBlank()) {
                try {
                    String itemNameForPrompt = type.getDrug().getItemName() != null
                            ? type.getDrug().getItemName()
                            : type.getDrug().getItemSeq();
                    
                    String generated = chatGptService.generateDrugTypeDescription(
                            itemNameForPrompt,
                            type.getTypeCode() != null ? type.getTypeCode().name() : "",
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

        // 7. GPT 전반적 주의사항 요약
        String overallSummary;
        try {
            List<String> itemNames = drugs.stream().map(Drug::getItemName).filter(Objects::nonNull).toList();
            List<String> typeNames = types.stream().map(DrugType::getTypeName).filter(Objects::nonNull).distinct().toList();
            overallSummary = chatGptService.generateOverallCaution(itemNames, typeNames);
        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_SUMMARY_FAILED);
        }

        // 8. MemberHistory 생성 (AI 분석 완료 후 새로 생성되는 구조)
        Long savedHistoryId = null;
        if (memberId != null && photoId != null) {
            try {
                // DB에 실제 존재하는 Drug 엔티티 조회
                Drug linkedDrug = drugRepository.findByItemSeq(dedupItemSeqs.get(0))
                        .orElseThrow(() -> new CoreException(ErrorType.DRUG_NOT_FOUND));

                // 업로드된 사진 조회
                MemberPhoto photo = memberPhotoRepository.findById(photoId)
                        .orElseThrow(() -> new CoreException(ErrorType.PHOTO_NOT_FOUND));

                // 이미 해당 사진에 history가 연결되어 있다면 재사용
                if (photo.getHistoryId() != null) {
                    savedHistoryId = photo.getHistoryId().getHistoryId();
                } else {
                    // 기존 이력이 없을 때만 새로 생성
                    MemberHistory history = MemberHistory.builder()
                            .memberId(Member.builder().memberId(memberId).build())
                            .itemSeq(linkedDrug)
                            .gptCautionSummary(overallSummary)
                            .build();

                    memberHistoryRepository.save(history);
                    savedHistoryId = history.getHistoryId();

                    // 새로 생성된 history와 사진 연결
                    photo.linkHistory(history);
                    memberPhotoRepository.save(photo);
                }

            } catch (Exception e) {
                throw new CoreException(ErrorType.HISTORY_SAVE_FAILED, "복용 이력 저장 중 오류가 발생했습니다.");
            }
        }

        // 9. 응답 변환
        final String finalOverallSummary = overallSummary;
        final Map<String, MemberPhotoDto> finalPhotoByItemSeq = photoByItemSeq;
        Map<String, List<DrugType>> typesBySeq = types.stream()
                .collect(Collectors.groupingBy(dt -> dt.getDrug().getItemSeq()));

        List<DrugCautionResponse> responses = drugs.stream()
                .map(drug -> {
                    List<DrugType> t = typesBySeq.getOrDefault(drug.getItemSeq(), Collections.emptyList());
                    MemberPhotoDto photo = finalPhotoByItemSeq.get(drug.getItemSeq());
                    return DrugCautionResponse.of(drug, t, finalOverallSummary, photo);
                })
                .filter(Objects::nonNull)
                .toList();

        return DrugCautionResult.builder()
                .foundDrugs(responses)
                .missingItems(
                    dedupItemSeqs.stream()
                        .filter(seq -> !drugs.stream().map(Drug::getItemSeq).toList().contains(seq))
                        .toList()
                )
                .historyId(savedHistoryId)
                .build();
        
    }
    	
}
