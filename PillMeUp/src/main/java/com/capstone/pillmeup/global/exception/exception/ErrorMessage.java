package com.capstone.pillmeup.global.exception.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "에러 응답 본문")
public class ErrorMessage {
	
	@Schema(description = "에러 코드", example = "MEMBER_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "회원을 찾을 수 없습니다.")
    private String message;

    public ErrorMessage(ErrorType errorType) {
        this.code = errorType.name();
        this.message = errorType.getMessage();
    }
    
}
