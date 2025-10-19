package com.capstone.pillmeup.domain.user.dto.request;

import lombok.Data;

@Data
public class MemberPasswordRequest {

	private String currentPassword;
	private String newPassword;
	
}
