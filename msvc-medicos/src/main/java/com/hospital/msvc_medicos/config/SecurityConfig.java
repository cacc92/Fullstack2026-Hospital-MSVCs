package com.hospital.msvc_medicos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

// Seguridad del microservicio de medicos.
//
// Este servicio actua como "Resource Server": no hace login ni emite tokens (eso es de msvc-usuarios),
// solo RECIBE peticiones que traen un JWT en la cabecera "Authorization: Bearer <token>", verifica que
// el token sea valido y decide, segun el rol, si deja pasar o no.
//
// Es la segunda barrera de seguridad: aunque alguien evite el gateway y llame directo al puerto 8001,
// igual tiene que presentar un token valido y con el rol correcto.
@Configuration
public class SecurityConfig {

    // Se lee del application.properties. Es la MISMA clave con la que msvc-usuarios firmo el token;
    // por eso aqui podemos verificar que la firma es autentica.
    @Value("${jwt.secret}")
    private String secret;

    // Construye la clave HMAC-SHA256 a partir del texto del secreto.
    // HS256 es un algoritmo "simetrico": la misma clave sirve para firmar y para verificar.
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        // El decoder verifica DOS cosas en cada peticion: que la firma del token sea correcta
        // (no fue alterado) y que no este vencido (claim exp). Si algo falla, responde 401.
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // El token guarda los roles en un claim llamado "roles" (ej: ["ROLE_MEDICO"]).
    // Spring necesita convertir esos textos en "authorities" para poder evaluarlos con hasRole(...).
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles"); // de que claim del token leer los roles
        authorities.setAuthorityPrefix("");           // sin prefijo extra: ya guardamos "ROLE_..." completo
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    // Aqui se define la cadena de filtros de seguridad: las reglas de quien puede entrar a que.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http
                // CSRF protege formularios web con sesion. Como esta es una API sin estado (token en cada
                // request) no aplica, asi que se desactiva para no bloquear los POST/PUT/DELETE.
                .csrf(csrf -> csrf.disable())
                // Las reglas se evaluan EN ORDEN: la primera que coincide con la peticion manda.
                .authorizeHttpRequests(auth -> auth
                        // Documentacion y consola h2: abiertas (sin token) para poder mostrarlas en clase.
                        .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        // LEER (GET): lo permite cualquier usuario autenticado con uno de estos roles.
                        // Va PRIMERO que la regla de abajo para que los GET no caigan en la regla de escritura.
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/**", "/api/v2/medicos/**")
                        .hasAnyRole("ADMIN", "MEDICO", "PACIENTE")
                        // ESCRIBIR (todo lo que no sea GET sobre esas rutas: POST/PUT/DELETE): solo ADMIN o MEDICO.
                        .requestMatchers("/api/v1/medicos/**", "/api/v2/medicos/**")
                        .hasAnyRole("ADMIN", "MEDICO")
                        // Cualquier otra ruta no listada: basta con estar autenticado.
                        .anyRequest().authenticated())
                // STATELESS: el servidor NO guarda sesion. Cada peticion se autentica sola con su token.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Activa la validacion del JWT usando el decoder y el conversor de roles de arriba.
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                // Permite que la consola h2 (que usa frames) se muestre en el navegador.
                .headers(h -> h.frameOptions(f -> f.disable()));
        return http.build();
    }
}
