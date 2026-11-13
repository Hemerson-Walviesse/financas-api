package com.apkfinancas.financas_api.dto;

import com.apkfinancas.financas_api.model.Gasto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        Long categoriaId,
        String categoriaNome) {

    public static GastoResponse from(Gasto g) {
        var cat = g.getCategoria();
        return new GastoResponse(g.getId(), g.getDescricao(), g.getValor(), g.getData(),
                cat == null ? null : cat.getId(),
                cat == null ? null : cat.getNome());
    }
}
