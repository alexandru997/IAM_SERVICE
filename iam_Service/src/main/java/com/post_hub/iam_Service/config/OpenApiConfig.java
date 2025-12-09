package com.post_hub.iam_Service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "'POST_HUB' REST API",
                version = "1.0",
                description = """
                        'IAM-service' - is the authentication and registration service. It allows users to:
                        - Authenticate
                        - Register
                        - Create new users
                        - Create posts
                        - Write comments to posts

                        Created by Alexandru Besliu | [Send me email](mailto:besliualexandru33@gmail.com)""",
                contact = @Contact(name = "POST_HUB")
        ),
        security = { @SecurityRequirement(name = HttpHeaders.AUTHORIZATION) }
)
@SecurityScheme(
        name = HttpHeaders.AUTHORIZATION,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    @Value("${swagger.servers.first}")
    private String firstServer;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("iam-service")
                .packagesToScan("com.post_hub.iam_Service.controller")
                .addOpenApiCustomizer(serverCustomizer())
                .build();
    }

    @Bean
    public OpenApiCustomizer serverCustomizer() {
        return openApi -> {
            List<Server> servers = new ArrayList<>();
            if (Objects.nonNull(firstServer)) {
                servers.add(new Server().url(firstServer).description("API Server"));
            }
            openApi.servers(servers);
        };
    }
}
