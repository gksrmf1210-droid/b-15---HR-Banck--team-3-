package com.hrbank.hrbank.global.config;


import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI hrBankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HR Bank API")
                        .description("HR Bank 프로젝트 API 문서")
                        .version("v1.0.0"));
    }






}
