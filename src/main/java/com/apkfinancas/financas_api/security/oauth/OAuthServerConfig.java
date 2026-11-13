package com.apkfinancas.financas_api.security.oauth;

import com.apkfinancas.financas_api.service.AuthService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.time.Duration;
import java.util.List;

/**
 * Servidor de autorização OAuth 2.1 usado pelo conector do Claude.
 *
 * Fluxo: Claude descobre o servidor (/.well-known/...), "registra" o cliente (/oauth2/register),
 * manda o usuário para /oauth2/authorize -> /login, recebe o código e troca por um token (/oauth2/token).
 * O token (JWT RS256) traz no "sub" o id do usuário, igual ao token do app.
 */
@Configuration
public class OAuthServerConfig {

    public static final String CLIENT_ID = "claude";
    public static final String ESCOPO = "mcp";

    /** Únicos endereços para onde o código pode voltar: o Claude e o MCP Inspector (testes locais). */
    public static final List<String> REDIRECTS_PERMITIDOS = List.of(
            "https://claude.ai/api/mcp/auth_callback",
            "https://claude.com/api/mcp/auth_callback",
            "http://localhost:6274/oauth/callback",
            "http://localhost:6274/oauth/callback/debug");

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient.Builder claude = RegisteredClient.withId(CLIENT_ID)
                .clientId(CLIENT_ID)
                .clientName("Claude")
                // cliente público: sem segredo, protegido por PKCE
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(ESCOPO)
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                        // mesmo prazo do token do app; depois disso o Claude pede para reconectar
                        .accessTokenTimeToLive(Duration.ofDays(30))
                        .build());
        REDIRECTS_PERMITIDOS.forEach(claude::redirectUri);
        return new InMemoryRegisteredClientRepository(claude.build());
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(ChaveOAuth chave) {
        return new ImmutableJWKSet<>(new JWKSet(chave.rsa()));
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(@Value("${app.url}") String appUrl) {
        return AuthorizationServerSettings.builder().issuer(semBarraFinal(appUrl)).build();
    }

    /** Coloca o id do usuário no "sub" do token, para as ferramentas MCP saberem de quem são os dados. */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenComIdDoUsuario(AuthService auth) {
        return ctx -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(ctx.getTokenType())) {
                ctx.getClaims().subject(idDoUsuario(ctx.getPrincipal(), auth).toString());
            }
        };
    }

    private static Long idDoUsuario(Authentication login, AuthService auth) {
        Object p = login.getPrincipal();
        if (p instanceof UsuarioPrincipal usuario) {
            return usuario.getId();
        }
        if (p instanceof OidcUser google) {
            if (!Boolean.TRUE.equals(google.getEmailVerified())) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("access_denied", "E-mail do Google não verificado", null));
            }
            return auth.encontrarOuCriarPorGoogle(google.getSubject(), google.getEmail(), google.getFullName()).getId();
        }
        throw new IllegalStateException("Tipo de login não suportado: " + p.getClass().getName());
    }

    public static String semBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
