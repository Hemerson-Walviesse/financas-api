package com.apkfinancas.financas_api.service;

import com.apkfinancas.financas_api.dto.GastoRequest;
import com.apkfinancas.financas_api.dto.GastoResponse;
import com.apkfinancas.financas_api.exception.RecursoNaoEncontradoException;
import com.apkfinancas.financas_api.model.Gasto;
import com.apkfinancas.financas_api.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository repo;
    private final CategoriaService categorias;
    private final UsuarioService usuarios;

    @Transactional(readOnly = true)
    public List<GastoResponse> listar(Long usuarioId, YearMonth mes) {
        return repo.findByUsuarioIdAndDataBetweenOrderByData(
                        usuarioId, mes.atDay(1), mes.atEndOfMonth()).stream()
                .map(GastoResponse::from)
                .toList();
    }

    @Transactional
    public GastoResponse criar(Long usuarioId, GastoRequest req) {
        Gasto g = new Gasto();
        g.setUsuario(usuarios.referencia(usuarioId));
        aplicar(usuarioId, g, req);
        return GastoResponse.from(repo.save(g));
    }

    @Transactional
    public GastoResponse atualizar(Long usuarioId, Long id, GastoRequest req) {
        Gasto g = buscar(usuarioId, id);
        aplicar(usuarioId, g, req);
        return GastoResponse.from(g);
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        repo.delete(buscar(usuarioId, id));
    }

    private Gasto buscar(Long usuarioId, Long id) {
        return repo.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gasto", id));
    }

    private void aplicar(Long usuarioId, Gasto g, GastoRequest req) {
        g.setDescricao(req.descricao().trim());
        g.setValor(req.valor());
        g.setData(req.data());
        g.setCategoria(categorias.buscarOpcional(usuarioId, req.categoriaId()));
    }
}
