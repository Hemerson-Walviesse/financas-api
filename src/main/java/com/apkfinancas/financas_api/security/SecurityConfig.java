package com.apkfinancas.financas_api.security;

import com.apkfinancas.financas_api.security.oauth.ChaveOAuth;
import com.apkfinancas.financas_api.security.oauth.OAuthServerConfig;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Três "portas" de segurança, em ordem:
 *  1. Servidor de autorização OAuth (para o Claude)      /oauth2/authorize, /oauth2/token, /.well-known/oauth-authorization-server...
 *  2. Página de login no navegador                        /login (e-mail e senha ou Google)
 *  3. API e MCP, sem sessão, com token Bearer            /api/**, /mcp
 */
@Configuration
public class SecurityConfig {

    static final String ISSUER = "financas-api";

    private final String appUrl;
    private final String googleClientId;
    private final String googleClientSecret;

    public SecurityConfig(@Value("${app.url}") String appUrl,
                          @Value("${app.google.client-id:}") String googleClientId,
                          @Value("${app.google.client-secret:}") String googleClientSecret) {
        this.appUrl = OAuthServerConfig.semBarraFinal(appUrl);
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
    }

    // ---------- 1. Servidor de autorização ----------
    @Bean
    @Order(1)
    public SecurityFilterChain servidorDeAutorizacao(HttpSecurity http) throws Exception {
        http
                .oauth2AuthorizationServer(as -> {
                    http.securityMatcher(as.getEndpointsMatcher());
                    as.authorizationServerMetadataEndpoint(meta -> meta.authorizationServerMetadataCustomizer(
                            b -> b.claim("registration_endpoint", appUrl + "/oauth2/register")));
                })
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                // sem login, o navegador vai para a nossa página /login
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    // ---------- 2. Página de login (navegador, com sessão) ----------
    @Bean
    @Order(2)
    public SecurityFilterChain paginaDeLogin(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/login", "/oauth2/authorization/**", "/login/oauth2/code/**")
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .defaultSuccessUrl("/login?conectado")
                        .failureUrl("/login?erro"));

        if (googleWebAtivo()) {
            var google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .build();
            http.oauth2Login(o -> o
                    .loginPage("/login")
                    .clientRegistrationRepository(new InMemoryClientRegistrationRepository(google))
                    .defaultSuccessUrl("/login?conectado")
                    .failureUrl("/login?erro-google"));
        }
        return http.build();
    }

    // ---------- 3. API REST e MCP (sem sessão) ----------
    @Bean
    @Order(3)
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint naoAutenticado = entradaComMetadados();
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/registrar", "/api/auth/login", "/api/auth/google",
                                "/api/ping", "/error", "/.well-known/**", "/oauth2/register").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(naoAutenticado))
                .oauth2ResourceServer(o -> o
                        .authenticationEntryPoint(naoAutenticado)
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * 401 com o endereço dos metadados (RFC 9728). É assim que o Claude descobre,
     * a partir do /mcp, onde fica o login.
     */
    private AuthenticationEntryPoint entradaComMetadados() {
        BearerTokenAuthenticationEntryPoint padrao = new BearerTokenAuthenticationEntryPoint();
        String metadados = "resource_metadata=\"" + appUrl + "/.well-known/oauth-protected-resource\"";
        return (req, res, ex) -> {
            padrao.commence(req, res, ex);
            String atual = res.getHeader("WWW-Authenticate");
            res.setHeader("WWW-Authenticate", (atual == null || atual.equals("Bearer"))
                    ? "Bearer " + metadados
                    : atual + ", " + metadados);
        };
    }

    public boolean googleWebAtivo() {
        return !googleClientId.isBlank() && !googleClientSecret.isBlank();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret precisa ter pelo menos 32 caracteres");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    /**
     * Aceita os dois tipos de token:
     *  - HS256, emitido pela própria API para o app (issuer "financas-api");
     *  - RS256, emitido pelo servidor de autorização para o Claude (issuer = APP_URL).
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKey, ChaveOAuth chaveOAuth) throws JOSEException {
        NimbusJwtDecoder doApp = NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
        doApp.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));

        NimbusJwtDecoder doClaude = NimbusJwtDecoder.withPublicKey(chaveOAuth.rsa().toRSAPublicKey()).build();
        doClaude.setJwtValidator(JwtValidators.createDefaultWithIssuer(appUrl));

        return token -> {
            String alg;
            try {
                alg = JWTParser.parse(token).getHeader().getAlgorithm().getName();
            } catch (ParseException e) {
                throw new BadJwtException("Token mal formado", e);
            }
            return "HS256".equals(alg) ? doApp.decode(token) : doClaude.decode(token);
        };
    }
}
