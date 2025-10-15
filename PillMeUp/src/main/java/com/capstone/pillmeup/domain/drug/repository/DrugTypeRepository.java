package com.capstone.pillmeup.domain.drug.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.pillmeup.domain.drug.entity.DrugType;

public interface DrugTypeRepository extends JpaRepository<DrugType, Long> {

}
