package com.capstone.pillmeup.global.exception.response;

import com.capstone.pillmeup.global.exception.exception.ErrorMessage;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "공통 응답 래퍼")
public class ApiResponse<T> {

	@Schema(description = "결과 상태", example = "SUCCESS")
    private ResultType result;

    @Schema(description = "성공 데이터 페이로드")
    private T data;

    @Schema(description = "에러 정보(실패 시 포함)")
    private ErrorMessage error;

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<?> error(ErrorType errorType) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorType));
    }

}
