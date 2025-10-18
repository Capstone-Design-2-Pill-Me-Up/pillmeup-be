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

    /**
     * AI 분석 결과 기반 약품 주의사항 전체 조회
     * @param request AI 모델이 반환한 의약품 item_seq 리스트
     * @param memberId 로그인 사용자 ID
     * @param photoId 해당 촬영 이미지 ID
     */
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
        List<String> foundItemSeqs = drugs.stream()
                .map(Drug::getItemSeq)
                .toList();

        List<String> missingItems = dedupItemSeqs.stream()
                .filter(seq -> !foundItemSeqs.contains(seq))
                .toList();

        if (drugs.isEmpty()) {
            throw new CoreException(ErrorType.DRUG_NOT_FOUND, "요청한 모든 의약품이 존재하지 않습니다.");
        }

        // 4. DUR 주의사항 조회
        List<DrugType> types = drugTypeRepository.findByDrug_ItemSeqIn(dedupItemSeqs);
        if (types.isEmpty()) {
            throw new CoreException(ErrorType.DRUG_TYPE_NOT_FOUND);
        }

        // 5. 회원 사진 조회 (Optional)
        Map<String, MemberPhotoDto> photoByItemSeq = new HashMap<>();
        if (memberId != null) {
            List<MemberPhoto> photos = memberPhotoRepository.findByMemberId_MemberIdAndItemSeq_ItemSeqIn(memberId, dedupItemSeqs);
            if (photos == null) {
                throw new CoreException(ErrorType.PHOTO_NOT_FOUND);
            }

            photoByItemSeq = photos.stream().collect(Collectors.toMap(
                    p -> p.getItemSeq().getItemSeq(),
                    p -> MemberPhotoDto.builder()
                            .photoId(p.getPhotoId())
                            .historyId(p.getHistoryId() != null ? p.getHistoryId().getHistoryId() : null)
                            .memberId(p.getMemberId() != null ? p.getMemberId().getMemberId() : null)
                            .itemSeq(p.getItemSeq() != null ? p.getItemSeq().getItemSeq() : null)
                            .fileName(p.getFileName())
                            .fileUrl(p.getFileUrl()) // TODO: S3 연동 전 null
                            .detectedName(p.getDetectedName())
                            .confidence(p.getConfidence())
                            .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                            .build(),
                    (a, b) -> a // 중복 키시 첫 번째 유지
            ));
        }

        // 6. GPT 개별 DUR 주의사항 보완
        for (DrugType type : types) {
            if (type.getDescription() == null || type.getDescription().isBlank()) {
                try {
                    String itemNameForPrompt = type.getDrug().getItemName() != null
                            ? type.getDrug().getItemName()
                            : type.getDrug().getItemSeq(); // fallback 처리

                    String generated = chatGptService.generateDrugTypeDescription(
                            itemNameForPrompt,
                            type.getTypeCode() != null ? type.getTypeCode().name() : "",
                            type.getTypeName()
                    );

                    type.setDescription(generated);
                    drugTypeRepository.save(type);

                } catch (Exception e) {
                    throw new CoreException(ErrorType.DRUG_CAUTION_GENERATION_FAILED,
                            "GPT 요청 실패 (" + type.getDrug().getItemSeq() + "): " + e.getMessage());
                }
            }
        }

        // 7. GPT 전반적 주의사항 요약
        String overallSummary;
        try {
            List<String> itemNames = drugs.stream().map(Drug::getItemName).filter(Objects::nonNull).toList();
            List<String> typeNames = types.stream().map(DrugType::getTypeName).filter(Objects::nonNull).distinct().toList();

            overallSummary = itemNames.isEmpty()
                    ? "의약품 이름이 존재하지 않아 요약할 수 없습니다."
                    : chatGptService.generateOverallCaution(itemNames, typeNames);

        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_SUMMARY_FAILED);
        }

        // 8. 조회 이력 저장
        if (memberId != null) {
            try {
                String itemSeqForHistory = null;

                if (photoId != null) {
                    MemberPhoto photo = memberPhotoRepository.findById(photoId)
                            .orElseThrow(() -> new CoreException(ErrorType.PHOTO_NOT_FOUND));
                    if (photo.getItemSeq() != null) {
                        itemSeqForHistory = photo.getItemSeq().getItemSeq();
                    }
                }

                if (itemSeqForHistory == null && !dedupItemSeqs.isEmpty()) {
                    itemSeqForHistory = dedupItemSeqs.get(0);
                }

                if (itemSeqForHistory != null) {
                    MemberHistory history = MemberHistory.builder()
                            .memberId(Member.builder().memberId(memberId).build())
                            .itemSeq(Drug.builder().itemSeq(itemSeqForHistory).build())
                            .gptCautionSummary(overallSummary)
                            .build();
                    memberHistoryRepository.save(history);
                }
            } catch (CoreException e) {
                throw e; // 명시적 예외 재전달
            } catch (Exception e) {
                throw new CoreException(ErrorType.HISTORY_SAVE_FAILED);
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
                .missingItems(missingItems)
                .build();
        
    }
	
}
