package com.capstone.pillmeup.domain.ai.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.capstone.pillmeup.domain.ai.dto.GptRequest;
import com.capstone.pillmeup.domain.ai.dto.GptResponse;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatGptService {

	private final WebClient openAiWebClient;

    @Value("${openai.model:gpt-4o}")
    private String model;

    // 전반적인 주의사항 요약 생성
    public String generateOverallCaution(List<String> itemNames, List<String> typeNames) {
    	
        String names = String.join(", ", itemNames);
        String types = String.join(", ", typeNames);

        String prompt = String.format("""
            사용자가 복용하려는 여러 의약품의 이름과 DUR(의약품 안전성) 주의사항이 다음과 같습니다.
            - 의약품 목록: %s
            - DUR 주의사항 종류: %s

            위 정보를 참고하여 이 약들을 동시에 복용할 때 전반적으로 주의해야 할 점을
            두 문장 이내로 간결하게 요약해줘.

            작성 규칙:
            1. 존칭은 사용하지 않는다.
            2. "~해야 합니다." 형태의 의학적 경고 문체 사용.
            3. 중복 표현 없이 핵심 경고 위주로 요약.

            예시:
            - "임부와 고령자는 복용을 피하고, 첨가제 과민 반응에 주의해야 합니다."
            - "여러 약물 병용 시 부작용 및 상호작용 위험이 있으므로 전문가 상담 후 복용해야 합니다."
        """, names, types);

        return requestToGpt(prompt);
        
    }
    
    // 개별 약품 상세정보 보완용 (drug/drug_type)
    public String generateDetailField(String itemName, String entpName, String fieldName) {
    	
        String prompt = String.format("""
            다음은 특정 의약품의 정보입니다.
            - 의약품명: %s
            - 제조사: %s
            - 요청 필드: %s

            위 정보를 참고하여 '%s' 항목에 들어갈 적절한 내용을 한국어로 한 문장 또는 간단한 단락으로 작성해줘.
            불필요한 수식어는 생략하고, 공백 없이 핵심 내용만 전달해.
            존칭은 사용하지 않는다.

            예시:
            - 효능/효과: "두통, 근육통, 생리통 등의 완화에 사용합니다."
            - 주의사항: "과량 복용 시 간 손상 위험이 있으므로 용량을 초과하지 않습니다."
            - 부작용: "피부 발진, 소화불량, 두통 등이 나타날 수 있습니다."
        """, itemName, entpName, fieldName, fieldName);

        return requestToGpt(prompt);
        
    }
    
    // DUR 주의사항(description) 생성
    public String generateDrugTypeDescription(String itemName, String typeCode, String typeName) {
    	
        String prompt = String.format("""
            다음은 특정 DUR 주의사항 코드 정보입니다.
            - 의약품명: %s
            - DUR 코드: %s
            - DUR 주의사항명: %s

            이 항목에 해당하는 복용 시 주의사항을 한 문장으로 작성해줘.
            존칭은 쓰지 말고, 의학적 경고 문체로 끝내.
            예시:
            - "임부는 복용을 피하고 필요한 경우 전문의 상담 후 사용해야 합니다."
            - "첨가제 과민 반응이 있는 환자는 복용을 피해야 합니다."
        """, itemName, typeCode, typeName);

        return requestToGpt(prompt);
        
    }
    
    // GPT 요청 처리
    private String requestToGpt(String prompt) {
    	
        try {
            GptRequest request = GptRequest.builder()
                    .model(model)
                    .messages(List.of(
                            new GptRequest.Message("system", "너는 한국 의약품 복용 주의사항을 생성하는 전문가야."),
                            new GptRequest.Message("user", prompt)
                    ))
                    .build();

            GptResponse response = openAiWebClient.post()
                    .body(Mono.just(request), GptRequest.class)
                    .retrieve()
                    .bodyToMono(GptResponse.class)
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new CoreException(ErrorType.GPT_API_ERROR);
            }

            return response.getChoices().get(0).getMessage().getContent().trim();
        } catch (Exception e) {
            throw new CoreException(ErrorType.GPT_API_ERROR, e.getMessage());
        }
        
    }
	
}
