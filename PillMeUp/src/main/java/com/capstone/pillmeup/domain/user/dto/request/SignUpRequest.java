package com.capstone.pillmeup.domain.user.dto.request;

import lombok.Data;

@Data
public class SignUpRequest {

	private String email;
	private String password;	// 로컬 회원만 사용
	private String name;
	
}
