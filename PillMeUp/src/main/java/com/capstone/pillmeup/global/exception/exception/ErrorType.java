package com.capstone.pillmeup.global.exception.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

	// ──────────────── MEMBER ────────────────
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰이 존재하지 않습니다."),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "토큰이 유효하지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 멤버입니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 존재하는 멤버입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "이름은 비워둘 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다."),

    // ──────────────── PHOTO ────────────────
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 photoId에 대한 사진이 존재하지 않습니다."),
    PHOTO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 업로드 중 오류가 발생했습니다."),
    PHOTO_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다."),
    PHOTO_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 파일 크기가 허용 범위를 초과했습니다."),

    // ──────────────── DRUG ────────────────
    DRUG_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 의약품 정보를 찾을 수 없습니다."),
    DRUG_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 의약품의 DUR 주의사항이 존재하지 않습니다."),
    DRUG_CAUTION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GPT를 통한 약품 주의사항 생성 중 오류가 발생했습니다."),

    // ──────────────── HISTORY ────────────────
    HISTORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "복용 이력 저장 중 오류가 발생했습니다."),
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원의 복용 이력을 찾을 수 없습니다."),

    // ──────────────── EXTERNAL API ────────────────
    EXTERNAL_API_ERROR_KAKAO(HttpStatus.BAD_GATEWAY, "카카오 API 통신 중 오류가 발생했습니다."),
    EXTERNAL_API_ERROR_NAVER(HttpStatus.BAD_GATEWAY, "네이버 API 통신 중 오류가 발생했습니다."),
    
    // ──────────────── GPT ────────────────
    GPT_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GPT API 호출 중 오류가 발생했습니다."),
    GPT_DATA_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GPT가 유효한 데이터를 생성하지 못했습니다."),
    GPT_SUMMARY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 전반적인 요약 생성 중 오류가 발생했습니다."),

    // ──────────────── AI ────────────────
    AI_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "AI 서버 요청 중 오류가 발생했습니다."),
    AI_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버 응답 형식이 올바르지 않습니다."),
    AI_ITEMSEQ_NOT_FOUND(HttpStatus.NOT_FOUND, "AI 모델이 item_seq를 반환하지 못했습니다."),
    AI_SERVER_COMMUNICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버와 통신 중 알 수 없는 오류가 발생했습니다."),

    // ──────────────── COMMON ────────────────
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 내부 오류입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 금지되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 처리 중 오류가 발생했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.")
    
    ;
	
	private final HttpStatus status;
	private final String message;
	
}
