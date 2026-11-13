package com.apkfinancas.financas_api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record ResumoMensalResponse(
        YearMonth mes,
        BigDecimal totalGastos,
        List<TotalPorCategoria> gastosPorCategoria,
        BigDecimal totalContasEmAberto,
        int quantidadeContasEmAberto,
        List<ContaResponse> proximosVencimentos) {

    public record TotalPorCategoria(String categoria, BigDecimal total) {}
}
