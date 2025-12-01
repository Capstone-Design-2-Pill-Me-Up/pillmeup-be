package com.capstone.pillmeup.domain.drug.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capstone.pillmeup.domain.drug.entity.Drug;
import com.capstone.pillmeup.domain.drug.entity.DrugType;
import com.capstone.pillmeup.domain.drug.repository.DrugRepository;
import com.capstone.pillmeup.domain.drug.repository.DrugTypeRepository;
import com.capstone.pillmeup.domain.history.dto.response.HistoryDetailResponse;
import com.capstone.pillmeup.domain.history.entity.MemberHistory;
import com.capstone.pillmeup.domain.history.repository.MemberHistoryRepository;
import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;
import com.capstone.pillmeup.domain.photo.repository.MemberPhotoRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrugHistoryDetailService {

	private final MemberHistoryRepository memberHistoryRepository;
    private final MemberPhotoRepository memberPhotoRepository;
    private final DrugRepository drugRepository;
    private final DrugTypeRepository drugTypeRepository;
	
    @Transactional
    public HistoryDetailResponse getHistoryDetail(Long historyId) {

        // 1. History 조회
        MemberHistory history = memberHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CoreException(ErrorType.HISTORY_NOT_FOUND));

        // 2. 해당 history의 모든 사진 가져오기
        List<MemberPhoto> photos = memberPhotoRepository.findByHistoryId_HistoryId(historyId);
        if (photos.isEmpty()) {
            throw new CoreException(ErrorType.PHOTO_NOT_FOUND);
        }

        // 3. 사진 URL 리스트
        List<String> fileUrls = photos.stream()
                .map(MemberPhoto::getFileUrl)
                .toList();

        // 4. 해당 history에서 감지된 모든 itemSeq
        List<String> itemSeqs = photos.stream()
                .map(p -> p.getItemSeq().getItemSeq())
                .distinct()
                .toList();

        // 5. 개별 약품 상세정보 구성
        List<DrugDetailResponse> drugDetails = itemSeqs.stream()
                .map(this::buildDrugDetail)
                .toList();

        // 6. 전체 변환
        return HistoryDetailResponse.builder()
                .historyId(historyId)
                .gptSummary(history.getGptCautionSummary())
                .imageUrls(fileUrls)
                .drugs(drugDetails)
                .build();
    }

    private DrugDetailResponse buildDrugDetail(String itemSeq) {

        Drug drug = drugRepository.findByItemSeq(itemSeq)
                .orElseThrow(() -> new CoreException(ErrorType.DRUG_NOT_FOUND));

        List<DrugType> types = drugTypeRepository.findByDrug_ItemSeq(itemSeq);

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

                .cautions(
                        types.stream()
                                .map(t -> DrugTypeResponse.builder()
                                        .typeCode(t.getTypeCode().name())
                                        .typeName(t.getTypeName())
                                        .description(t.getDescription())
                                        .level("high")
                                        .message(t.getDescription())
                                        .build()
                                ).collect(Collectors.toList())
                )
                .build();
    }
    
}
