package com.apkfinancas.financas_api.security.oauth;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/** Usuário autenticado na página de login (guarda o id para colocar no token). */
public class UsuarioPrincipal extends User {

    private final Long id;

    public UsuarioPrincipal(Long id, String email, String senhaHash) {
        // Conta só-Google não tem senha: um hash vazio nunca confere
        super(email, senhaHash == null ? "" : senhaHash, AuthorityUtils.createAuthorityList("ROLE_USER"));
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
