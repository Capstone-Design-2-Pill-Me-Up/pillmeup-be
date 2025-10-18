package com.capstone.pillmeup.domain.drug.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.drug.entity.Drug;

public interface DrugRepository extends JpaRepository<Drug, Long> {

	List<Drug> findByItemSeqIn(List<String> itemSeqList);
	Optional<Drug> findByItemSeq(String itemSeq);
	
}
