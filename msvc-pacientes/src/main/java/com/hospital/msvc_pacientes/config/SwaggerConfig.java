package com.hospital.msvc_pacientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration: Spring lee esta clase al arrancar y registra sus @Bean en el contexto.
@Configuration
public class SwaggerConfig {

    // @Bean: el objeto que retorna este metodo queda disponible para que Spring lo inyecte.
    // springdoc lo usa para construir la pagina de Swagger UI (ver springdoc.* en application.properties).
    @Bean
    public OpenAPI customOpenApi(){
        // Info es la cabecera que se muestra arriba en /docs/swagger-ui.html (titulo, version, descripcion).
        return new OpenAPI()
                .info(new Info()
                        .title("API Pacientes")
                        .version("1.0")
                        .description("Documentación de la API de gestión de pacientes")
                );
    }
}
