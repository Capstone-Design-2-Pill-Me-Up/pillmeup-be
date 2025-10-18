package com.capstone.pillmeup.domain.photo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;

public interface MemberPhotoRepository extends JpaRepository<MemberPhoto, Long> {

	List<MemberPhoto> findByMemberId_MemberIdAndItemSeq_ItemSeqIn(Long memberId, List<String> itemSeqs);

}
