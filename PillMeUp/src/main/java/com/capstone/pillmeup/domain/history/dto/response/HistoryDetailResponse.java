package com.capstone.pillmeup.domain.history.dto.response;

import java.util.List;

import com.capstone.pillmeup.domain.drug.dto.response.DrugDetailResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryDetailResponse {

	private Long historyId;
    private String gptSummary;
    private List<String> imageUrls;
    private List<DrugDetailResponse> drugs;
	
}
