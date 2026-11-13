package com.apkfinancas.financas_api.repository;

import com.apkfinancas.financas_api.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioIdOrderByNome(Long usuarioId);
    Optional<Categoria> findByIdAndUsuarioId(Long id, Long usuarioId);
    Optional<Categoria> findFirstByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}
