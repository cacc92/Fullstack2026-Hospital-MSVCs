package com.hospital.msvc_atenciones.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration: Spring lee esta clase al arrancar y registra sus @Bean en el contexto.
@Configuration
public class SwaggerConfig {

    private static final String ESQUEMA = "bearer-jwt";

    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("API Atenciones")
                        .version("1.0")
                        .description("Documentación de la API de gestión de atenciones"))
                // Esquema "bearer-jwt": agrega el boton Authorize para pegar el token JWT.
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Aplica el esquema a todos los endpoints, asi Swagger manda el token al probar.
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
