package com.capstone.pillmeup.domain.photo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import reactor.core.publisher.Mono;

// TODO: AI모델 개발 완료 시, 연동 예정
@Service
public class AiModelClient {

	private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:5000") // Flask 서버 예정
            .build();

    public String sendImageToAi(String imageUrl) {
        try {
            String result = webClient.post()
                    .uri("/analyze")
                    .bodyValue("{\"image_url\":\"" + imageUrl + "\"}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.just("AI_MODEL_PENDING"))
                    .block();

            return result != null ? result : "AI_MODEL_PENDING";

        } catch (Exception e) {
            throw new CoreException(ErrorType.AI_SERVER_COMMUNICATION_FAILED);
        }
    }
	
}
