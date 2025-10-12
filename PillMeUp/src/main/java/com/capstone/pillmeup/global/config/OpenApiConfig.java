package com.capstone.pillmeup.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME_NAME = "BearerAuth";

	@Bean
    public OpenAPI pillMeUpOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pill Me Up API")
                        .description("""
                                Vision AI 기반 의약품 식별 서비스의 백엔드 API 문서입니다.
                                - 응답은 항상 ApiResponse<T> 래퍼로 반환됩니다.
                                - JWT 인증이 필요한 엔드포인트는 우상단 'Authorize' 버튼으로 토큰을 입력하세요.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact().name("Pill Me Up Team"))
                        .license(new License().name("")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
	
}
