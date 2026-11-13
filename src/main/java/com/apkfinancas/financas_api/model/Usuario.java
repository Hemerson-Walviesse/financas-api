package com.apkfinancas.financas_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    /** Nulo para quem entrou só com o Google. */
    private String senhaHash;

    @Column(unique = true)
    private String googleId;

    @CreationTimestamp
    private Instant criadoEm;
}
