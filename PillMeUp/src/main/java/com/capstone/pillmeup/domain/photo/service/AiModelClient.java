package com.capstone.pillmeup.domain.photo.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

@Component
public class AiModelClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8000")
          //.baseUrl("https://wonsandbox.cloud")
            .build();

    public List<String> sendImageToAi(File imageFile) {

        try {
            FileSystemResource resource = new FileSystemResource(imageFile);

            Map<String, Object> response = webClient.post()
                    .uri("/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", resource))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        throw new CoreException(ErrorType.AI_REQUEST_FAILED);
                    })
                    .block();

            if (response == null || !response.containsKey("result")) {
                throw new CoreException(ErrorType.AI_RESPONSE_INVALID);
            }

            List<Map<String, Object>> predictions =
                    (List<Map<String, Object>>) response.get("result");

            if (predictions == null || predictions.isEmpty()) {
                throw new CoreException(ErrorType.AI_ITEMSEQ_NOT_FOUND);
            }

            List<String> itemSeqList = new ArrayList<>();

            for (Map<String, Object> pill : predictions) {
                Object itemSeq = pill.get("item_seq");
                if (itemSeq != null) {
                    itemSeqList.add(String.valueOf(itemSeq));
                }
            }

            if (itemSeqList.isEmpty()) {
                throw new CoreException(ErrorType.AI_ITEMSEQ_NOT_FOUND);
            }

            return itemSeqList;

        } catch (CoreException e) {
            throw e; // 이미 커스텀 예외면 그대로 던짐
        } catch (Exception e) {
            e.printStackTrace();
            throw new CoreException(ErrorType.AI_SERVER_COMMUNICATION_FAILED);
        }
    }
}
