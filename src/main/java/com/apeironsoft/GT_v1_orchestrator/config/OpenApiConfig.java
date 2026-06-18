package com.apeironsoft.GT_v1_orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gtV1OpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GT v1 Orchestrator API")
                        .version("v1")
                        .description("API documentation for GT v1 Orchestrator"));
    }
}
