package com.tenextakehome;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Tenex Take Home Assessment API",
                version = "1.0.0",
                description = "Spring Boot REST API with a clean service-layer architecture"
        )
)
public class TenexTakeHomeAssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenexTakeHomeAssessmentApplication.class, args);
    }
}
