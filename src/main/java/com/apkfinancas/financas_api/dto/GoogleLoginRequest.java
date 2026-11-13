package com.apkfinancas.financas_api.dto;

import jakarta.validation.constraints.NotBlank;

/** ID token que o app recebe do Google ao fazer login no celular. */
public record GoogleLoginRequest(@NotBlank String idToken) {}
