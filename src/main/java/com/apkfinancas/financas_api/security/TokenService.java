package com.apkfinancas.financas_api.security;

import com.apkfinancas.financas_api.model.Usuario;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

/** Tokens do APP (HS256). Os tokens do Claude são emitidos pelo Authorization Server (RS256). */
@Service
public class TokenService {

    public record Token(String valor, Instant expiraEm) {}

    private final JwtEncoder encoder;
    private final Duration validade;

    public TokenService(SecretKey jwtKey, @Value("${app.jwt.expiracao-dias}") long dias) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
        this.validade = Duration.ofDays(dias);
    }

    public Token gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(validade);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(SecurityConfig.ISSUER)
                .issuedAt(agora)
                .expiresAt(expira)
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(valor, expira);
    }
}
