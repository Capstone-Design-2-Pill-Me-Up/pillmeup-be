package com.capstone.pillmeup.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.user.entity.MemberPhoto;

public interface MemberPhotoRepository extends JpaRepository<MemberPhoto, Long> {

}
