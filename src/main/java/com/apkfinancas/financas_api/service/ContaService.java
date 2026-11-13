package com.apkfinancas.financas_api.service;

import com.apkfinancas.financas_api.dto.ContaRequest;
import com.apkfinancas.financas_api.dto.ContaResponse;
import com.apkfinancas.financas_api.exception.RecursoNaoEncontradoException;
import com.apkfinancas.financas_api.model.Conta;
import com.apkfinancas.financas_api.repository.ContaRepository;
import com.apkfinancas.financas_api.util.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository repo;
    private final CategoriaService categorias;
    private final UsuarioService usuarios;

    @Transactional(readOnly = true)
    public List<ContaResponse> listar(Long usuarioId, boolean somenteAbertas) {
        List<Conta> lista = somenteAbertas
                ? repo.findByUsuarioIdAndPagaFalseOrderByVencimento(usuarioId)
                : repo.findByUsuarioIdOrderByVencimentoDesc(usuarioId);
        return lista.stream().map(ContaResponse::from).toList();
    }

    /** Contas em aberto que vencem até daqui a N dias (inclui as atrasadas). */
    @Transactional(readOnly = true)
    public List<ContaResponse> proximas(Long usuarioId, int dias) {
        return repo.findByUsuarioIdAndPagaFalseAndVencimentoLessThanEqualOrderByVencimento(
                        usuarioId, Datas.hoje().plusDays(dias)).stream()
                .map(ContaResponse::from)
                .toList();
    }

    @Transactional
    public ContaResponse criar(Long usuarioId, ContaRequest req) {
        Conta c = new Conta();
        c.setUsuario(usuarios.referencia(usuarioId));
        aplicar(usuarioId, c, req);
        return ContaResponse.from(repo.save(c));
    }

    @Transactional
    public ContaResponse atualizar(Long usuarioId, Long id, ContaRequest req) {
        Conta c = buscar(usuarioId, id);
        aplicar(usuarioId, c, req);
        return ContaResponse.from(c);
    }

    /** Marca como paga. Se for recorrente, já cria a conta do mês seguinte. */
    @Transactional
    public ContaResponse pagar(Long usuarioId, Long id) {
        Conta c = buscar(usuarioId, id);
        if (c.isPaga()) {
            return ContaResponse.from(c);
        }
        c.setPaga(true);
        c.setPagaEm(Datas.hoje());

        if (c.isRecorrente()) {
            Conta proxima = new Conta();
            proxima.setUsuario(c.getUsuario());
            proxima.setDescricao(c.getDescricao());
            proxima.setValor(c.getValor());
            proxima.setCategoria(c.getCategoria());
            proxima.setRecorrente(true);
            proxima.setVencimento(c.getVencimento().plusMonths(1));
            repo.save(proxima);
        }
        return ContaResponse.from(c);
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        repo.delete(buscar(usuarioId, id));
    }

    private Conta buscar(Long usuarioId, Long id) {
        return repo.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta", id));
    }

    private void aplicar(Long usuarioId, Conta c, ContaRequest req) {
        c.setDescricao(req.descricao().trim());
        c.setValor(req.valor());
        c.setVencimento(req.vencimento());
        c.setRecorrente(req.recorrente());
        c.setCategoria(categorias.buscarOpcional(usuarioId, req.categoriaId()));
    }
}
