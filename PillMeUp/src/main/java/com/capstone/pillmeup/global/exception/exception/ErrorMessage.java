package com.capstone.pillmeup.global.exception.exception;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "에러 응답 본문")
public class ErrorMessage {
	
	@Schema(description = "HTTP 상태 코드", example = "404")
    private int status;

    @Schema(description = "에러 코드", example = "MEMBER_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "존재하지 않는 멤버입니다.")
    private String message;

    @Schema(description = "발생 시각", example = "2025-10-18T15:45:32")
    private LocalDateTime timestamp;

    public ErrorMessage(ErrorType errorType) {
        this.status = errorType.getStatus().value();
        this.code = errorType.name();
        this.message = errorType.getMessage();
        this.timestamp = LocalDateTime.now();
    }
    
    // 기본 생성 (ErrorType 기반)
    public static ErrorMessage of(ErrorType errorType) {
        return ErrorMessage.builder()
                .status(errorType.getStatus().value())
                .code(errorType.name())
                .message(errorType.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 상세 메시지 포함
    public static ErrorMessage of(ErrorType errorType, String detail) {
        return ErrorMessage.builder()
                .status(errorType.getStatus().value())
                .code(errorType.name())
                .message(detail != null ? detail : errorType.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
