package com.apkfinancas.financas_api.repository;

import com.apkfinancas.financas_api.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findByUsuarioIdOrderByVencimentoDesc(Long usuarioId);
    List<Conta> findByUsuarioIdAndPagaFalseOrderByVencimento(Long usuarioId);
    List<Conta> findByUsuarioIdAndPagaFalseAndVencimentoLessThanEqualOrderByVencimento(Long usuarioId, LocalDate ate);
    Optional<Conta> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByCategoriaId(Long categoriaId);
}
