package com.capstone.pillmeup.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SignUpRequest {

	@Schema(description = "이메일(로그인 ID)", example = "user1@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "비밀번호(로컬 가입 전용)", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
	
}
