package com.hospital.msvc_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

// Seguridad del GATEWAY: la PRIMERA barrera.
//
// Como todo el trafico entra por aqui, el gateway revisa que cada peticion traiga un token valido
// ANTES de enrutarla a los microservicios. Si no hay token o es invalido, corta con 401 y nunca llega
// al servicio. Esto se llama AUTENTICACION ("¿quien eres y tu token es legitimo?").
//
// Importante: el gateway NO decide aqui quien puede hacer que segun el rol; esa AUTORIZACION fina
// (leer vs escribir, etc.) la aplica cada microservicio. Asi cada uno queda protegido por si mismo.
// El gateway reenvia la cabecera "Authorization" al servicio destino, asi que el token llega completo.
@Configuration
public class SecurityConfig {

    // Misma clave secreta que uso msvc-usuarios para firmar: por eso el gateway puede validar la firma.
    @Value("${jwt.secret}")
    private String secret;

    // Verifica firma y vencimiento del token en cada peticion (HS256, clave simetrica compartida).
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Lee los roles del token. Aqui no se usan para autorizar, pero quedan disponibles por si en el
    // futuro se quisiera filtrar algo a nivel de gateway.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http
                // API sin estado: el token viaja en cada request, no hay formularios con sesion. CSRF off.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Login y registro DEBEN ser publicos: el usuario aun no tiene token cuando los llama.
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Swagger agregado del gateway y los docs de cada servicio: abiertos para la demo.
                        .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/webjars/**").permitAll()
                        // Cualquier otra ruta exige un token valido (si no, 401 y no se rutea).
                        .anyRequest().authenticated())
                // El gateway tampoco guarda sesion: cada peticion se valida sola con su token.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Activa la validacion del JWT (decoder + lectura de roles) antes de enrutar.
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
        return http.build();
    }
}
