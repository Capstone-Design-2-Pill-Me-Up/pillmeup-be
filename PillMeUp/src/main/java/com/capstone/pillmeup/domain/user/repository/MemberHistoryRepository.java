package com.capstone.pillmeup.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.user.entity.MemberHistory;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {

}
