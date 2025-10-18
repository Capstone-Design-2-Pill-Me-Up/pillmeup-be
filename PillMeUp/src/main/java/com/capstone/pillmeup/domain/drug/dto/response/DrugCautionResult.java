package com.capstone.pillmeup.domain.drug.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DrugCautionResult {

	private List<DrugCautionResponse> foundDrugs;
    private List<String> missingItems;
	
}
