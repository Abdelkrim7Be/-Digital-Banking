package com.bellagnech.dig_bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// OpenAPI/Swagger Configuration for Digital Banking API - Provides comprehensive API documentation with examples and detailed descriptions
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digitalBankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Banking API")
                        .description("""
                                ## Digital Banking REST API

                                A comprehensive Spring Boot REST API for digital banking operations including:

                                ### Features:
                                - **Customer Management**: Create, read, update, and delete customer information
                                - **Account Management**: Support for Current and Saving accounts with different features
                                - **Banking Operations**: Deposit, withdrawal, and transfer operations
                                - **Account History**: Complete transaction history with pagination
                                - **Interest Calculation**: Automatic interest calculation for saving accounts
                                - **Account Status Management**: Activate, suspend, or close accounts

                                ### Account Types:
                                - **Current Account**: Supports overdraft facility for business operations
                                - **Saving Account**: Earns interest on the balance with configurable rates

                                ### Security:
                                This API is designed without authentication for demonstration purposes.
                                In production, implement proper authentication and authorization.

                                ### Error Handling:
                                The API provides comprehensive error responses with appropriate HTTP status codes
                                and detailed error messages for better debugging and user experience.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Digital Banking Development Team")
                                .email("dev@digitalbanking.com")
                                .url("https://github.com/bellagnech/digital-banking"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.digitalbanking.com")
                                .description("Production Server (Example)")
                ));
    }
}
