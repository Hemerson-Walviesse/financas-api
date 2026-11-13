package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.security.oauth.OAuthServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OAuthDescobertaController {

    private final String appUrl;

    public OAuthDescobertaController(@Value("${app.url}") String appUrl) {
        this.appUrl = OAuthServerConfig.semBarraFinal(appUrl);
    }

    /** Metadados do recurso protegido (RFC 9728): "o /mcp é protegido por este servidor de login". */
    @GetMapping(value = {"/.well-known/oauth-protected-resource", "/.well-known/oauth-protected-resource/mcp"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> recursoProtegido() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("resource", appUrl + "/mcp");
        m.put("authorization_servers", List.of(appUrl));
        m.put("scopes_supported", List.of(OAuthServerConfig.ESCOPO));
        m.put("bearer_methods_supported", List.of("header"));
        m.put("resource_name", "Finanças");
        return m;
    }

    /**
     * Registro dinâmico de cliente (RFC 7591), no modo mais simples e seguro para este app:
     * só aceita os endereços de retorno do Claude/Inspector e sempre devolve o mesmo
     * cliente público "claude" (sem segredo, com PKCE). Assim nada precisa ficar salvo
     * no banco e o registro sobrevive aos reinícios do servidor.
     */
    @PostMapping(value = "/oauth2/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Map<String, Object> pedido) {
        List<String> redirects = new ArrayList<>();
        if (pedido.get("redirect_uris") instanceof List<?> lista) {
            lista.forEach(u -> redirects.add(String.valueOf(u)));
        }
        if (redirects.isEmpty() || !OAuthServerConfig.REDIRECTS_PERMITIDOS.containsAll(redirects)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_redirect_uri",
                    "error_description", "Este servidor só aceita os endereços de retorno do Claude."));
        }
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("client_id", OAuthServerConfig.CLIENT_ID);
        cliente.put("client_id_issued_at", Instant.now().getEpochSecond());
        cliente.put("client_name", pedido.getOrDefault("client_name", "Claude"));
        cliente.put("redirect_uris", redirects);
        cliente.put("grant_types", List.of("authorization_code"));
        cliente.put("response_types", List.of("code"));
        cliente.put("token_endpoint_auth_method", "none");
        cliente.put("scope", OAuthServerConfig.ESCOPO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }
}
