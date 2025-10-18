package com.capstone.pillmeup.domain.drug.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.drug.entity.DrugType;

public interface DrugTypeRepository extends JpaRepository<DrugType, Long> {

	List<DrugType> findByDrug_ItemSeqIn(List<String> itemSeqs);
	List<DrugType> findByDrug_ItemSeq(String itemSeq);
	
}
