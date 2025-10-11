package com.capstone.pillmeup.global.exception.response;

import com.capstone.pillmeup.global.exception.exception.ErrorMessage;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

	private ResultType result;
    private T data;
    private ErrorMessage error;

    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static ApiResponse<?> error(ErrorType errorType) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorType));
    }

}
