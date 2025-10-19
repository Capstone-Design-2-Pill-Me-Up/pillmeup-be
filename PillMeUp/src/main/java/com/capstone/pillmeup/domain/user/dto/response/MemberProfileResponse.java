package com.capstone.pillmeup.domain.user.dto.response;

import com.capstone.pillmeup.domain.user.entity.Provider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberProfileResponse {

	private String email;
	private String name;
	private Provider provider;
	
}
