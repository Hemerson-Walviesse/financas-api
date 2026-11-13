package com.apkfinancas.financas_api.dto;

import com.apkfinancas.financas_api.model.Conta;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContaResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate vencimento,
        boolean paga,
        LocalDate pagaEm,
        boolean recorrente,
        Long categoriaId,
        String categoriaNome) {

    public static ContaResponse from(Conta c) {
        var cat = c.getCategoria();
        return new ContaResponse(c.getId(), c.getDescricao(), c.getValor(), c.getVencimento(),
                c.isPaga(), c.getPagaEm(), c.isRecorrente(),
                cat == null ? null : cat.getId(),
                cat == null ? null : cat.getNome());
    }
}
