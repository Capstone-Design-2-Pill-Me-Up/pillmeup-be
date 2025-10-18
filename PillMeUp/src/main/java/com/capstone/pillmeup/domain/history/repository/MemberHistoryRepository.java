package com.capstone.pillmeup.domain.history.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.history.entity.MemberHistory;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {
	
}
