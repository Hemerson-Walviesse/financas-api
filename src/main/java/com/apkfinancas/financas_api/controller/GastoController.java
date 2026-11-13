package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.dto.GastoRequest;
import com.apkfinancas.financas_api.dto.GastoResponse;
import com.apkfinancas.financas_api.security.UsuarioLogado;
import com.apkfinancas.financas_api.service.GastoService;
import com.apkfinancas.financas_api.util.Datas;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService service;

    /** ?mes=2026-11 (padrão: mês atual) */
    @GetMapping
    public List<GastoResponse> listar(@UsuarioLogado Long usuarioId,
                                      @RequestParam(required = false) YearMonth mes) {
        return service.listar(usuarioId, mes != null ? mes : Datas.mesAtual());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoResponse criar(@UsuarioLogado Long usuarioId,
                               @Valid @RequestBody GastoRequest req) {
        return service.criar(usuarioId, req);
    }

    @PutMapping("/{id}")
    public GastoResponse atualizar(@UsuarioLogado Long usuarioId,
                                   @PathVariable Long id,
                                   @Valid @RequestBody GastoRequest req) {
        return service.atualizar(usuarioId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@UsuarioLogado Long usuarioId, @PathVariable Long id) {
        service.excluir(usuarioId, id);
    }
}
