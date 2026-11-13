package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.dto.GoogleLoginRequest;
import com.apkfinancas.financas_api.dto.LoginRequest;
import com.apkfinancas.financas_api.dto.RegistroRequest;
import com.apkfinancas.financas_api.dto.TokenResponse;
import com.apkfinancas.financas_api.dto.UsuarioResponse;
import com.apkfinancas.financas_api.security.UsuarioLogado;
import com.apkfinancas.financas_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/google")
    public TokenResponse google(@Valid @RequestBody GoogleLoginRequest req) {
        return service.loginGoogle(req);
    }

    @GetMapping("/eu")
    public UsuarioResponse eu(@UsuarioLogado Long usuarioId) {
        return service.eu(usuarioId);
    }
}
