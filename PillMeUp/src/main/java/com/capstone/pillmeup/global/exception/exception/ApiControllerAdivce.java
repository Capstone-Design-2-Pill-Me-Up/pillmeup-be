package com.capstone.pillmeup.global.exception.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.capstone.pillmeup.global.exception.response.ApiResponse;

@RestControllerAdvice
public class ApiControllerAdivce {

	// 커스텀 예외 처리
	@ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorMessage> handleCoreException(CoreException e) {
        ErrorType type = e.getErrorType();
        return ResponseEntity
                .status(type.getStatus())
                .body(ErrorMessage.of(type, e.getDetailMessage()));
    }
    
    // 예상치 못한 예외 처리
	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorType.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorMessage.of(ErrorType.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
	
}
