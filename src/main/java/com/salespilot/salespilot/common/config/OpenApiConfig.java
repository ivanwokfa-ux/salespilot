package com.salespilot.salespilot.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salesPilotOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SalesPilot API")
                .version("v1")
                .description("Import, nettoyage, analyse et reporting des ventes de petites entreprises."));
    }
}