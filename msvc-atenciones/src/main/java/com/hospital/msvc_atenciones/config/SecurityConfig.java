package com.hospital.msvc_atenciones.config;

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

// Seguridad del microservicio de atenciones.
//
// Es un "Resource Server": recibe peticiones con un JWT en "Authorization: Bearer <token>", lo valida
// y autoriza segun el rol. No emite tokens (eso es de msvc-usuarios). Segunda barrera: protege el
// servicio aunque alguien llame directo al puerto 8002 sin pasar por el gateway.
@Configuration
public class SecurityConfig {

    // Misma clave secreta con la que se firmo el token; sirve para verificar su firma.
    @Value("${jwt.secret}")
    private String secret;

    // En cada peticion verifica que la firma sea correcta y que el token no este vencido (claim exp).
    // HS256 es simetrico: la misma clave firma y verifica.
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Convierte el claim "roles" del token en authorities para poder evaluarlos con hasRole(...).
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles"); // claim de donde leer los roles
        authorities.setAuthorityPrefix("");           // sin prefijo: ya vienen como "ROLE_..."
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http
                // API sin estado (token en cada request): CSRF no aplica, se desactiva.
                .csrf(csrf -> csrf.disable())
                // Las reglas se evaluan EN ORDEN; la primera que coincide gana.
                .authorizeHttpRequests(auth -> auth
                        // Documentacion y consola h2 abiertas para la demo.
                        .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        // LEER atenciones (GET): cualquier rol autenticado, incluido PACIENTE.
                        // Va antes que la regla de escritura para que los GET no caigan en ella.
                        .requestMatchers(HttpMethod.GET, "/api/v1/atenciones/**")
                        .hasAnyRole("ADMIN", "MEDICO", "PACIENTE")
                        // ESCRIBIR atenciones (POST/PUT/DELETE): solo ADMIN o MEDICO.
                        .requestMatchers("/api/v1/atenciones/**")
                        .hasAnyRole("ADMIN", "MEDICO")
                        .anyRequest().authenticated())
                // Sin sesion en el servidor: cada peticion se autentica con su propio token.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Activa la validacion del JWT con el decoder y el conversor de roles.
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                // Permite ver la consola h2 (usa frames) en el navegador.
                .headers(h -> h.frameOptions(f -> f.disable()));
        return http.build();
    }
}
