package com.capstone.pillmeup.global.exception.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.capstone.pillmeup.global.exception.response.ApiResponse;

@RestControllerAdvice
public class ApiControllerAdivce {

	// 커스텀 예외 처리
    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<?>> handleCoreException(CoreException e) {
        return ResponseEntity
                .status(e.getErrorType().getStatus())
                .body(ApiResponse.error(e.getErrorType()));
    }
    
    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorType.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(ErrorType.INTERNAL_SERVER_ERROR));
    }
	
}
