package com.hospital.msvc_medicos.config;

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

    // Nombre interno del esquema de seguridad; se referencia desde el requerimiento global.
    private static final String ESQUEMA = "bearer-jwt";

    // @Bean: springdoc usa este objeto para construir la pagina de Swagger UI.
    @Bean
    public OpenAPI customOpenApi(){
        return new OpenAPI()
                // Info: cabecera que se muestra arriba en /docs/swagger-ui.html.
                .info(new Info()
                        .title("API Medicos")
                        .version("1.0")
                        .description("Documentación de la API de gestión de médicos"))
                // Define el esquema "bearer-jwt": hace aparecer el boton Authorize en Swagger,
                // donde se pega el token JWT obtenido en /api/v1/auth/login.
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)   // autenticacion por cabecera HTTP
                                .scheme("bearer")                 // formato "Authorization: Bearer <token>"
                                .bearerFormat("JWT")))
                // Aplica ese esquema a TODOS los endpoints: asi Swagger envia el token al ejecutar
                // "Try it out". Sin esto, con la seguridad activa toda prueba devolveria 401.
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
