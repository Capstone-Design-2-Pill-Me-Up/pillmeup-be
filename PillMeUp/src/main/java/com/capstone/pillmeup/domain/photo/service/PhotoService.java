package com.capstone.pillmeup.domain.photo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.capstone.pillmeup.domain.drug.entity.Drug;
import com.capstone.pillmeup.domain.drug.repository.DrugRepository;
import com.capstone.pillmeup.domain.history.entity.MemberHistory;
import com.capstone.pillmeup.domain.history.repository.MemberHistoryRepository;
import com.capstone.pillmeup.domain.photo.dto.response.PhotoUploadResponse;
import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;
import com.capstone.pillmeup.domain.photo.repository.MemberPhotoRepository;
import com.capstone.pillmeup.domain.user.entity.Member;
import com.capstone.pillmeup.domain.user.repository.MemberRepository;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoService {

	private final S3Service s3Service;
	private final AiModelClient aiModelClient;
	private final MemberPhotoRepository memberPhotoRepository;
	private final MemberHistoryRepository memberHistoryRepository;
	private final MemberRepository memberRepository;
	private final DrugRepository drugRepository;

    // 사진 업로드 + AI 모델 호출 + DB 저장 전체 자동 처리
    @Transactional
    public PhotoUploadResponse uploadAndAnalyze(Long memberId, MultipartFile file) {

        // 1. 사용자 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 이미지 파일 검증
        validateImage(file);

        // 3. S3 업로드
        String fileUrl = s3Service.uploadFile(file);

        // 4. temp 파일 생성 (FastAPI multipart 전송용)
        File tempFile = convertToTempFile(file);

        // 5. AI 서버 호출 → item_seq 리스트 반환
        List<String> itemSeqList = aiModelClient.sendImageToAi(tempFile)
                .stream().distinct().toList();

        tempFile.delete(); // temp 파일 제거

        // 6. MemberHistory 생성
        MemberHistory history = MemberHistory.builder()
                .memberId(member)
                .gptCautionSummary(null)
                .createdAt(LocalDateTime.now())
                .build();
        memberHistoryRepository.save(history);

        // 7. 감지된 알약 개수만큼 MemberPhoto 저장
        for (String seq : itemSeqList) {
            Drug drug = getOrCreateDrug(seq);

            MemberPhoto photo = MemberPhoto.builder()
                    .historyId(history)
                    .memberId(member)
                    .itemSeq(drug)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .detectedName(seq)
                    .confidence(1.0)
                    .createdAt(LocalDateTime.now())
                    .build();

            memberPhotoRepository.save(photo);
        }

        // 8. 응답 반환
        return PhotoUploadResponse.builder()
                .fileUrl(fileUrl)
                .detectedName(itemSeqList.isEmpty() ? null : itemSeqList.get(0))
                .confidence("1.0000")
                .message("AI 분석 완료")
                .itemSeqList(itemSeqList)
                .build();
        
    }

    // 이미지 파일 검증
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new CoreException(ErrorType.PHOTO_NOT_FOUND);

        if (!file.getContentType().startsWith("image/"))
            throw new CoreException(ErrorType.PHOTO_INVALID_FORMAT);

        if (file.getSize() > 5 * 1024 * 1024) // 5MB 제한
            throw new CoreException(ErrorType.PHOTO_SIZE_EXCEEDED);
    }

    // MultipartFile → temp File 변환
    private File convertToTempFile(MultipartFile file) {
        try {
            File convFile = File.createTempFile("pill_", ".png");
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }
            return convFile;
        } catch (Exception e) {
            throw new CoreException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    // item_seq → Drug 조회 or 생성
    private Drug getOrCreateDrug(String itemSeq) {

        // AI 결과 없음 → PENDING 저장
        if (itemSeq == null) {
            return drugRepository.findByItemSeq("AI_PENDING")
                    .orElseGet(() -> drugRepository.save(
                            Drug.builder()
                                    .itemSeq("AI_PENDING")
                                    .itemName("AI 분석 대기중")
                                    .entpName("N/A")
                                    .build()
                    ));
        }

        // 실제 item_seq가 DB에 존재하면 그대로 사용
        return drugRepository.findByItemSeq(itemSeq)
                .orElseGet(() -> drugRepository.save(
                        Drug.builder()
                                .itemSeq(itemSeq)
                                .itemName("AI 분석 약품")
                                .entpName("AI 인식 결과")
                                .build()
                ));
    }
	
}
