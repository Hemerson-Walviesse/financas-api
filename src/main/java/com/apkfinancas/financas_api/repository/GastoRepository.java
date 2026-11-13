package com.apkfinancas.financas_api.repository;

import com.apkfinancas.financas_api.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findByUsuarioIdAndDataBetweenOrderByData(Long usuarioId, LocalDate de, LocalDate ate);
    Optional<Gasto> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByCategoriaId(Long categoriaId);
}
