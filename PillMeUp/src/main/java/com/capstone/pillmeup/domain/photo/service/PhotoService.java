package com.capstone.pillmeup.domain.photo.service;

import java.time.LocalDateTime;

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

	@Transactional
	public PhotoUploadResponse uploadAndAnalyze(Long memberId, MultipartFile file) {

		// 1️. 사용자 확인
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		// 2️. 파일 유효성 검사
		if (file == null || file.isEmpty()) {
			throw new CoreException(ErrorType.PHOTO_NOT_FOUND);
		}
		if (!file.getContentType().startsWith("image/")) {
			throw new CoreException(ErrorType.PHOTO_INVALID_FORMAT);
		}
		if (file.getSize() > 5 * 1024 * 1024) { // 5MB 제한
			throw new CoreException(ErrorType.PHOTO_SIZE_EXCEEDED);
		}

		// 3️. S3 업로드
		String fileUrl;
		try {
			fileUrl = s3Service.uploadFile(file);
		} catch (Exception e) {
			throw new CoreException(ErrorType.PHOTO_UPLOAD_FAILED);
		}

		// 4️. AI 모델 서버로 전달 (현재 Mock)
		String aiResult;
		try {
			aiResult = aiModelClient.sendImageToAi(fileUrl);
		} catch (Exception e) {
			aiResult = "AI_MODEL_PENDING"; // AI 서버 연동 실패 시 기본값
		}

		// 5️. 임시 약품 데이터 설정 (AI 분석 전 단계)
		Drug pendingDrug = drugRepository.findByItemSeq("AI_PENDING")
                .orElseGet(() -> {
                    Drug newDrug = Drug.builder()
                            .itemSeq("AI_PENDING")
                            .itemName("AI 분석 대기중")
                            .entpName("N/A")
                            .build();
                    return drugRepository.save(newDrug);
                });

		// 6️. member_history 저장
		MemberHistory history;
		try {
			history = MemberHistory.builder()
					.memberId(member)
					.itemSeq(pendingDrug)
					.gptCautionSummary(null)
					.createdAt(LocalDateTime.now())
					.build();
			memberHistoryRepository.save(history);
		} catch (Exception e) {
			throw new CoreException(ErrorType.HISTORY_SAVE_FAILED);
		}

		// 7️. member_photo 저장
		try {
			MemberPhoto photo = MemberPhoto.builder()
					.historyId(history)
					.memberId(member)
					.itemSeq(pendingDrug)
					.fileName(file.getOriginalFilename())
					.fileUrl(fileUrl)
					.detectedName(aiResult)
					.confidence(null)
					.createdAt(LocalDateTime.now())
					.build();
			memberPhotoRepository.save(photo);
		} catch (Exception e) {
			throw new CoreException(ErrorType.PHOTO_UPLOAD_FAILED);
		}

		// 8️. 응답 반환
		return PhotoUploadResponse.builder()
				.fileUrl(fileUrl)
				.detectedName(aiResult)
				.confidence("0.0000")
				.message("이미지 업로드 및 분석 준비 완료")
				.build();
		
	}
    
}
