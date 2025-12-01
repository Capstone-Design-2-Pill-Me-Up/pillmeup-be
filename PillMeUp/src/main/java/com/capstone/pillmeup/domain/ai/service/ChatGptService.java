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
            다음은 사용자가 동시에 섭취한 여러 의약품 목록입니다.
            - 약품명 목록: %s
            - 해당 약물들에서 확인된 주의사항 종류: %s

            이 약물들을 함께 복용할 때 발생할 수 있는 대표적인 약물 상호작용(약물-약물 상호작용),
            주요 부작용 강화, 장기 기능 영향(간/신장), 중추신경계 영향 등
            실제 약리학적 관점에서 발생 가능한 위험성을 중심으로 종합적으로 설명해줘.

            작성 규칙:
            1. 'DUR', 'e약은요' 등 시스템/플랫폼 명칭 언급 금지.
            2. 단일 품목 설명이 아니라, 반드시 "여러 약물 간 상호작용"을 중심으로 설명한다.
            3. 예측/일반적 위험성을 약리작용 기반으로 2~4문장 정도로 작성.
            4. '~할 수 있습니다.', '~유발할 수 있으므로 주의해야 합니다.'와 같은 전문 보고체 유지.
            5. 특정 질환자(임부, 고혈압, 간질환 등)에 대한 주의사항도 포함 가능.

            예시:
            - "교감신경계를 자극하는 성분과 중추신경계 억제제가 함께 복용될 경우 심박수 변화와 졸음·호흡 저하가 동시에 나타날 수 있습니다."
            - "간 대사를 거치는 약물들이 여러 개 포함되어 있어 체내 농도가 높아지면 간 기능에 부담을 줄 수 있으므로 주의가 필요합니다."
        """, names, types);

        return requestToGpt(prompt);
        
    }
    
    // 개별 약품 상세정보 보완용 (drug/drug_type)
    public String generateDetailField(String itemName, String entpName, String fieldName) {
    	
        String prompt = String.format("""
            다음은 특정 의약품의 기본 정보입니다.
        - 의약품명: %s
        - 제조사: %s
        - 요청 항목: %s

        위 약품의 '%s'에 해당하는 설명을 자연스러운 한국어 문장으로 작성해줘.

        작성 규칙:
        1. '-' 기호나 '•' 등의 불릿 기호는 절대 사용하지 않는다.
        2. 문장 사이 줄바꿈(\\n)은 사용하지 말고, 모든 내용을 하나의 단락으로 작성한다.
        3. 문장 수는 2~4문장 이내로 제한한다.
        4. '효능·효과:', '용법·용량:' 등 제목은 포함하지 않는다.
        5. 지나치게 전문적인 용어, 화학 구조 설명, 수치는 생략한다.
        6. '~에 사용합니다.', '~할 수 있습니다.', '~주의해야 합니다.' 등의 객관적이고 간결한 서술체로 마무리한다.

        예시 (참고용):
        - 기관지 확장과 혈압 상승 작용을 통해 천식 및 저혈압 치료에 사용합니다. 고혈압이나 심장 질환이 있는 환자는 복용 전 전문가와 상담해야 합니다.
        - 감기의 여러 증상(기침, 발열, 근육통 등)을 완화하는 데 사용합니다. 과다 복용 시 간 손상 위험이 있으므로 정해진 용량을 지켜야 합니다.
        """, itemName, entpName, fieldName, fieldName);

        return requestToGpt(prompt);
        
    }
    
    // DUR 주의사항(description) 생성
    public String generateDrugTypeDescription(String itemName, String typeCode, String typeName) {
    	
        String prompt = String.format("""
            아래는 특정 의약품의 주의사항 정보입니다.
	        - 의약품명: %s
	        - 주의사항 코드: %s
	        - 주의사항 이름: %s
	
	        이 약품이 해당 주의사항에 속하는 이유를 간단히 설명해줘.
	        약리학적 이유(성분이나 작용)를 1문장 정도 포함하고,
	        왜 주의해야 하는지를 일반인도 이해할 수 있게 작성한다.
	        문장은 2~3문장 이내로 구성하고 '~해야 합니다.' 형태로 마무리한다.
	
	        예시:
	        - "%%s은(는) 교감신경을 자극하여 혈압을 높일 수 있으므로 임부는 복용을 피해야 합니다."
	        - "%%s은(는) 중추신경계에 작용해 졸음이나 어지럼을 유발할 수 있으므로 운전 시 주의해야 합니다."
	        - "%%s은(는) 간 대사를 거쳐 체내 농도가 높아질 수 있으므로 간 질환자는 주의가 필요합니다."
        """, itemName, typeCode, typeName);

        return requestToGpt(prompt);
        
    }
    
    // GPT 요청 처리
    private String requestToGpt(String prompt) {
    	
        try {
            GptRequest request = GptRequest.builder()
                    .model(model)
                    .messages(List.of(
                            new GptRequest.Message("system", "너는 한국의 약학 전문가이자 의약품 안전 가이드 작성자야. 일반 사용자가 이해할 수 있도록 약물 작용을 간단하고 명확히 설명해야 한다."),
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
