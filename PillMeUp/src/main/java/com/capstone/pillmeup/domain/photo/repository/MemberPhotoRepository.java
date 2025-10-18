package com.capstone.pillmeup.domain.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.photo.entity.MemberPhoto;

public interface MemberPhotoRepository extends JpaRepository<MemberPhoto, Long> {

}
