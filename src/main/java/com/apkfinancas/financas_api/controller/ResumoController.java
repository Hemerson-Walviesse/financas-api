package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.dto.ResumoMensalResponse;
import com.apkfinancas.financas_api.security.UsuarioLogado;
import com.apkfinancas.financas_api.service.ResumoService;
import com.apkfinancas.financas_api.util.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/resumo")
@RequiredArgsConstructor
public class ResumoController {

    private final ResumoService service;

    /** ?mes=2026-11 (padrão: mês atual) */
    @GetMapping
    public ResumoMensalResponse resumo(@UsuarioLogado Long usuarioId,
                                       @RequestParam(required = false) YearMonth mes) {
        return service.resumo(usuarioId, mes != null ? mes : Datas.mesAtual());
    }
}
