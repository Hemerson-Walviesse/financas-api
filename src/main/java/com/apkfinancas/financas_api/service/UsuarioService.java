package com.apkfinancas.financas_api.service;

import com.apkfinancas.financas_api.exception.RecursoNaoEncontradoException;
import com.apkfinancas.financas_api.model.Usuario;
import com.apkfinancas.financas_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;

    /** Garante que o usuário existe e devolve uma referência leve para relacionamentos. */
    public Usuario referencia(Long usuarioId) {
        if (!repo.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário", usuarioId);
        }
        return repo.getReferenceById(usuarioId);
    }
}
