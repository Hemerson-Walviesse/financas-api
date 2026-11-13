package com.apkfinancas.financas_api.service;

import com.apkfinancas.financas_api.dto.CategoriaRequest;
import com.apkfinancas.financas_api.dto.CategoriaResponse;
import com.apkfinancas.financas_api.exception.ConflitoException;
import com.apkfinancas.financas_api.exception.RecursoNaoEncontradoException;
import com.apkfinancas.financas_api.model.Categoria;
import com.apkfinancas.financas_api.repository.CategoriaRepository;
import com.apkfinancas.financas_api.repository.ContaRepository;
import com.apkfinancas.financas_api.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repo;
    private final ContaRepository contas;
    private final GastoRepository gastos;
    private final UsuarioService usuarios;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(Long usuarioId) {
        return repo.findByUsuarioIdOrderByNome(usuarioId).stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    @Transactional
    public CategoriaResponse criar(Long usuarioId, CategoriaRequest req) {
        Categoria c = new Categoria();
        c.setNome(req.nome().trim());
        c.setUsuario(usuarios.referencia(usuarioId));
        return CategoriaResponse.from(repo.save(c));
    }

    @Transactional
    public CategoriaResponse renomear(Long usuarioId, Long id, CategoriaRequest req) {
        Categoria c = buscar(usuarioId, id);
        c.setNome(req.nome().trim());
        return CategoriaResponse.from(c);
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        Categoria c = buscar(usuarioId, id);
        if (contas.existsByCategoriaId(id) || gastos.existsByCategoriaId(id)) {
            throw new ConflitoException("Categoria em uso por contas ou gastos");
        }
        repo.delete(c);
    }

    Categoria buscar(Long usuarioId, Long id) {
        return repo.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    /** Categoria é opcional em contas e gastos. */
    Categoria buscarOpcional(Long usuarioId, Long id) {
        return id == null ? null : buscar(usuarioId, id);
    }

    /** Usado pelo MCP: o Claude fala o nome da categoria ("Mercado"), não o id. */
    @Transactional
    public Long buscarOuCriarPorNome(Long usuarioId, String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String limpo = nome.trim();
        return repo.findFirstByUsuarioIdAndNomeIgnoreCase(usuarioId, limpo)
                .map(Categoria::getId)
                .orElseGet(() -> criar(usuarioId, new CategoriaRequest(limpo)).id());
    }
}
