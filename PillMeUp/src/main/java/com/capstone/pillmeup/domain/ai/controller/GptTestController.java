//package com.capstone.pillmeup.domain.ai.controller;
//
//import java.util.List;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.capstone.pillmeup.domain.ai.service.ChatGptService;
//import com.capstone.pillmeup.global.exception.response.ApiResponse;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.ArraySchema;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/test/gpt")
//@Tag(name = "[TEST] GPT API 테스트", description = "ChatGPT를 이용한 약품 정보 및 주의사항 생성 테스트용 API")
//public class GptTestController {
//
//	private final ChatGptService chatGptService;
//
//    @Operation(
//        summary = "전반적인 주의사항 요약",
//        description = "여러 의약품 이름과 DUR 주의사항명을 입력받아 전반적인 복용 주의사항을 요약합니다."
//    )
//    @GetMapping("/overall")
//    public ApiResponse<String> generateOverallCaution(
//            @Parameter(
//                description = "의약품 이름 목록",
//                array = @ArraySchema(schema = @Schema(example = "활명수, 아네모정, 부광덱사메타손정"))
//            )
//            @RequestParam("itemNames") List<String> itemNames,
//            @Parameter(
//                description = "DUR 주의사항명 목록",
//                array = @ArraySchema(schema = @Schema(example = "임부금기, 첨가제주의, 노인주의"))
//            )
//            @RequestParam("typeNames") List<String> typeNames) {
//
//        return ApiResponse.success(chatGptService.generateOverallCaution(itemNames, typeNames));
//    }
//
//    @Operation(
//        summary = "개별 약품 필드 생성",
//        description = "특정 의약품의 누락된 필드(효능/용법/주의사항 등)를 GPT로 생성합니다."
//    )
//    @GetMapping("/detail")
//    public ApiResponse<String> generateDetailField(
//            @Parameter(description = "의약품명", example = "활명수")
//            @RequestParam("itemName") String itemName,
//            @Parameter(description = "제조사명", example = "동화약품(주)")
//            @RequestParam("entpName") String entpName,
//            @Parameter(description = "생성할 필드명 (efcy_qesitm, atpn_qesitm 등)", example = "efcy_qesitm")
//            @RequestParam("fieldName") String fieldName) {
//
//        return ApiResponse.success(chatGptService.generateDetailField(itemName, entpName, fieldName));
//    }
//
//    @Operation(
//        summary = "DUR 주의사항 생성",
//        description = "의약품명과 DUR 코드 및 주의사항명을 입력받아 GPT로 복용 주의사항 문장을 생성합니다."
//    )
//    @GetMapping("/type")
//    public ApiResponse<String> generateDrugTypeDescription(
//            @Parameter(description = "의약품명", example = "부광덱사메타손정")
//            @RequestParam("itemName") String itemName,
//            @Parameter(description = "DUR 코드", example = "C")
//            @RequestParam("typeCode") String typeCode,
//            @Parameter(description = "DUR 주의사항명", example = "임부금기")
//            @RequestParam("typeName") String typeName) {
//
//        return ApiResponse.success(chatGptService.generateDrugTypeDescription(itemName, typeCode, typeName));
//    }
//	
//}
