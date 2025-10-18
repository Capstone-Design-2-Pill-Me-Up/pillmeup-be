package com.capstone.pillmeup.domain.photo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;

public interface MemberPhotoRepository extends JpaRepository<MemberPhoto, Long> {

	// 특정 이력(historyId) + 회원(memberId) 기준으로 사진 전체 조회
    List<MemberPhoto> findByHistoryId_HistoryIdAndMemberId_MemberId(Long historyId, Long memberId);

    // 특정 이력 기준 최신 1건 (상세보기에서 사용)
    Optional<MemberPhoto> findTopByHistoryId_HistoryIdOrderByCreatedAtDesc(Long historyId);
	
}
