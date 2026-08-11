package com.microlend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MicroLend API")
                        .version("1.0")
                        .description("Microfinance Loan Management System APIs")
                        .contact(new Contact()
                                .name("MicroLend Team")
                                .email("support@microlend.com")));
    }
}