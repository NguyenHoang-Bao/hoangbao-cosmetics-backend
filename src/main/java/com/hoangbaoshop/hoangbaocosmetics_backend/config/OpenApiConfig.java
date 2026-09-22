package com.hoangbaoshop.hoangbaocosmetics_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Hoang Bao Cosmetics E-Commerce API")
                        .version("1.0.0")
                        .description("Tài liệu & Giao diện thử nghiệm RESTful API hoàn chỉnh cho dự án Mỹ phẩm Hoàng Bảo.")
                        .contact(new Contact().name("Hoang Bao Cosmetics").email("support@hoangbaocosmetics.com"))
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT Access Token để xác thực các API yêu cầu quyền đăng nhập.")
                        )
                );
    }
}
