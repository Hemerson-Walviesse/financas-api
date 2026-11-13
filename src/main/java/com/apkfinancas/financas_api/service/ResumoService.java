package com.apkfinancas.financas_api.service;

import com.apkfinancas.financas_api.dto.ContaResponse;
import com.apkfinancas.financas_api.dto.GastoResponse;
import com.apkfinancas.financas_api.dto.ResumoMensalResponse;
import com.apkfinancas.financas_api.dto.ResumoMensalResponse.TotalPorCategoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumoService {

    private static final String SEM_CATEGORIA = "Sem categoria";

    private final GastoService gastos;
    private final ContaService contas;

    @Transactional(readOnly = true)
    public ResumoMensalResponse resumo(Long usuarioId, YearMonth mes) {
        List<GastoResponse> gastosDoMes = gastos.listar(usuarioId, mes);

        BigDecimal total = soma(gastosDoMes.stream().map(GastoResponse::valor).toList());

        Map<String, BigDecimal> porCategoria = gastosDoMes.stream()
                .collect(Collectors.groupingBy(
                        g -> Objects.requireNonNullElse(g.categoriaNome(), SEM_CATEGORIA),
                        Collectors.reducing(BigDecimal.ZERO, GastoResponse::valor, BigDecimal::add)));

        List<TotalPorCategoria> categorias = porCategoria.entrySet().stream()
                .map(e -> new TotalPorCategoria(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(TotalPorCategoria::total).reversed())
                .toList();

        List<ContaResponse> abertas = contas.listar(usuarioId, true);
        BigDecimal totalAberto = soma(abertas.stream().map(ContaResponse::valor).toList());

        return new ResumoMensalResponse(
                mes,
                total,
                categorias,
                totalAberto,
                abertas.size(),
                contas.proximas(usuarioId, 7));
    }

    private static BigDecimal soma(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
