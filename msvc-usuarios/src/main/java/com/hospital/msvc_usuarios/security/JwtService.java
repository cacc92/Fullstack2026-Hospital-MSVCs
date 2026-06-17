package com.hospital.msvc_usuarios.security;

import com.hospital.msvc_usuarios.models.Rol;
import com.hospital.msvc_usuarios.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Genera (FIRMA) el JWT que el resto del sistema validara.
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    // Minutos de validez del token (configurable). Por defecto 60 min.
    @Value("${jwt.expiration-minutes:60}")
    private long expirationMinutes;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        // Los roles se firman dentro del token como una lista de strings (ROLE_X).
        List<String> roles = usuario.getRoles().stream().map(Rol::getNombre).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("msvc-usuarios")                       // quien emitio el token
                .issuedAt(ahora)                               // cuando
                .expiresAt(ahora.plus(expirationMinutes, ChronoUnit.MINUTES)) // hasta cuando vale
                .subject(usuario.getUsername())                // a quien pertenece
                .claim("roles", roles)                         // permisos
                .build();

        // Cabecera: algoritmo HMAC-SHA256 (clave secreta compartida).
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
