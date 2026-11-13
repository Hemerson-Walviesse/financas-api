package com.apkfinancas.financas_api.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(@NotBlank String nome) {}
