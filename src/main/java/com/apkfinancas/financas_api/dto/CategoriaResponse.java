package com.apkfinancas.financas_api.dto;

import com.apkfinancas.financas_api.model.Categoria;

public record CategoriaResponse(Long id, String nome) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome());
    }
}
