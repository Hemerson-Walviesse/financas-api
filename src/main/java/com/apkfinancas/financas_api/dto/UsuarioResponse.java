package com.apkfinancas.financas_api.dto;

import com.apkfinancas.financas_api.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail());
    }
}
