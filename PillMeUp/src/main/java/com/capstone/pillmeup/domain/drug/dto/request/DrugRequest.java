package com.capstone.pillmeup.domain.drug.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugRequest {

	private List<String> itemSeqList;
	
}
