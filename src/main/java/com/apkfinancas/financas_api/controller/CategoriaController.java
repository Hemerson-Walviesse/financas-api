package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.dto.CategoriaRequest;
import com.apkfinancas.financas_api.dto.CategoriaResponse;
import com.apkfinancas.financas_api.security.UsuarioLogado;
import com.apkfinancas.financas_api.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @GetMapping
    public List<CategoriaResponse> listar(@UsuarioLogado Long usuarioId) {
        return service.listar(usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@UsuarioLogado Long usuarioId,
                                   @Valid @RequestBody CategoriaRequest req) {
        return service.criar(usuarioId, req);
    }

    @PutMapping("/{id}")
    public CategoriaResponse renomear(@UsuarioLogado Long usuarioId,
                                      @PathVariable Long id,
                                      @Valid @RequestBody CategoriaRequest req) {
        return service.renomear(usuarioId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@UsuarioLogado Long usuarioId, @PathVariable Long id) {
        service.excluir(usuarioId, id);
    }
}
