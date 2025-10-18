package com.capstone.pillmeup.global.exception.exception;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

	private final ErrorType errorType;
    private final String detailMessage;

    public CoreException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.detailMessage = errorType.getMessage();
    }

    public CoreException(ErrorType errorType, String detailMessage) {
        super(detailMessage);
        this.errorType = errorType;
        this.detailMessage = detailMessage != null ? detailMessage : errorType.getMessage();
    }
	
}
