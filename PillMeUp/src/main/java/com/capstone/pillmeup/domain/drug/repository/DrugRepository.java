package com.capstone.pillmeup.domain.drug.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.drug.entity.Drug;

public interface DrugRepository extends JpaRepository<Drug, Long> {

}
