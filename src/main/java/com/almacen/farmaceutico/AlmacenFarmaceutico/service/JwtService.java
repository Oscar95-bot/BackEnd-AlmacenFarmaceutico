package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.LoginResponse;
import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.UsuarioResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {
    private static final String ISSUER = "almacen-farmaceutico";
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Duration expiration;
    // El reinicio invalida todos los tokens, también los revocados almacenados en memoria.
    private final String instancia = UUID.randomUUID().toString();
    // Las revocaciones solo necesitan conservarse hasta la caducidad del token.
    private final ConcurrentHashMap<String, Instant> revocados = new ConcurrentHashMap<>();

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration:30m}") Duration expiration) {
        if (expiration.isNegative() || expiration.isZero()) {
            throw new IllegalArgumentException("La duración JWT debe ser positiva.");
        }
        this.expiration = expiration;
        byte[] bytes;
        if (secret.isBlank()) {
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = Base64.getDecoder().decode(secret);
            if (bytes.length < 32) throw new IllegalArgumentException("La clave JWT requiere al menos 32 bytes.");
        }
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator(ISSUER)));
        decoder = jwtDecoder;
    }

    public LoginResponse emitir(UsuarioResponse usuario) {
        limpiarRevocados();
        Instant ahora = Instant.now();
        Instant vence = ahora.plus(expiration);
        var claims = JwtClaimsSet.builder().issuer(ISSUER).subject(usuario.usuario())
                .issuedAt(ahora).expiresAt(vence).id(UUID.randomUUID().toString())
                .claim("usuarioId", usuario.id()).claim("rol", usuario.rol().name())
                .claim("instancia", instancia).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", vence, usuario);
    }

    public Jwt validar(String token) {
        limpiarRevocados();
        Jwt jwt = decoder.decode(token);
        if (jwt.getSubject() == null || jwt.getId() == null || jwt.getExpiresAt() == null
                || !jwt.getExpiresAt().isAfter(Instant.now()) || revocados.containsKey(jwt.getId())
                || !instancia.equals(jwt.getClaimAsString("instancia"))) {
            throw new BadJwtException("Token inválido, vencido o revocado.");
        }
        return jwt;
    }

    public void revocar(Jwt jwt) {
        limpiarRevocados();
        revocados.put(jwt.getId(), jwt.getExpiresAt());
    }

    private void limpiarRevocados() {
        Instant ahora = Instant.now();
        revocados.entrySet().removeIf(entry -> !entry.getValue().isAfter(ahora));
    }
}
