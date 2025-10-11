package com.capstone.pillmeup.domain.user.dto.request;

import lombok.Data;

@Data
public class SignInRequest {

	private String email;
	private String password;
	
}
