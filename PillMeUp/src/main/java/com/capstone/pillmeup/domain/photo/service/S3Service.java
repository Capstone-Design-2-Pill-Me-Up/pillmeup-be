package com.capstone.pillmeup.domain.photo.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.capstone.pillmeup.global.exception.exception.CoreException;
import com.capstone.pillmeup.global.exception.exception.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {

	private final AmazonS3 amazonS3;
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	
	public String uploadFile(MultipartFile file) {
        try {
            // 파일명 지정
            String fileName = "pill-images/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(
                    bucket,
                    fileName,
                    file.getInputStream(),
                    metadata
            );

            // 업로드 수행
            amazonS3.putObject(request);

            // 업로드 성공 시 URL 반환
            return amazonS3.getUrl(bucket, fileName).toString();

        } catch (IOException e) {
            throw new CoreException(ErrorType.PHOTO_UPLOAD_FAILED);
        } catch (Exception e) {
            throw new CoreException(ErrorType.PHOTO_UPLOAD_FAILED);
        }
    }
	
}
