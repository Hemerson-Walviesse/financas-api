package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.dto.ContaRequest;
import com.apkfinancas.financas_api.dto.ContaResponse;
import com.apkfinancas.financas_api.security.UsuarioLogado;
import com.apkfinancas.financas_api.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService service;

    @GetMapping
    public List<ContaResponse> listar(@UsuarioLogado Long usuarioId,
                                      @RequestParam(defaultValue = "false") boolean abertas) {
        return service.listar(usuarioId, abertas);
    }

    @GetMapping("/proximas")
    public List<ContaResponse> proximas(@UsuarioLogado Long usuarioId,
                                        @RequestParam(defaultValue = "7") int dias) {
        return service.proximas(usuarioId, dias);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponse criar(@UsuarioLogado Long usuarioId,
                               @Valid @RequestBody ContaRequest req) {
        return service.criar(usuarioId, req);
    }

    @PutMapping("/{id}")
    public ContaResponse atualizar(@UsuarioLogado Long usuarioId,
                                   @PathVariable Long id,
                                   @Valid @RequestBody ContaRequest req) {
        return service.atualizar(usuarioId, id, req);
    }

    @PatchMapping("/{id}/pagar")
    public ContaResponse pagar(@UsuarioLogado Long usuarioId, @PathVariable Long id) {
        return service.pagar(usuarioId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@UsuarioLogado Long usuarioId, @PathVariable Long id) {
        service.excluir(usuarioId, id);
    }
}
