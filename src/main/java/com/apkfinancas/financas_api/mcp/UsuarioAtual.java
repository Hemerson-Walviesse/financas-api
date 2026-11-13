package com.apkfinancas.financas_api.mcp;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Lê o usuário do token que veio na chamada MCP (o /mcp também exige Bearer). */
final class UsuarioAtual {

    private UsuarioAtual() {}

    static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return Long.valueOf(jwt.getToken().getSubject());
        }
        throw new IllegalStateException("Chamada MCP sem usuário autenticado");
    }
}
