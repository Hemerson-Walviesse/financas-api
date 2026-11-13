package com.apkfinancas.financas_api.service;

import com.apkfinancas.financas_api.dto.GoogleLoginRequest;
import com.apkfinancas.financas_api.dto.LoginRequest;
import com.apkfinancas.financas_api.dto.RegistroRequest;
import com.apkfinancas.financas_api.dto.TokenResponse;
import com.apkfinancas.financas_api.dto.UsuarioResponse;
import com.apkfinancas.financas_api.exception.ConflitoException;
import com.apkfinancas.financas_api.exception.CredenciaisInvalidasException;
import com.apkfinancas.financas_api.exception.RecursoNaoEncontradoException;
import com.apkfinancas.financas_api.model.Usuario;
import com.apkfinancas.financas_api.repository.UsuarioRepository;
import com.apkfinancas.financas_api.security.GoogleTokenVerifier;
import com.apkfinancas.financas_api.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final GoogleTokenVerifier google;

    @Transactional
    public TokenResponse registrar(RegistroRequest req) {
        String email = normalizar(req.email());
        if (repo.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(passwordEncoder.encode(req.senha()));
        return emitir(repo.save(u));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        Usuario u = repo.findByEmail(normalizar(req.email()))
                .filter(x -> x.getSenhaHash() != null && passwordEncoder.matches(req.senha(), x.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);
        return emitir(u);
    }

    /**
     * Login com Google: o app manda o ID token que recebeu do Google.
     * Se o e-mail já existe (cadastro com senha), a conta é vinculada ao Google.
     */
    @Transactional
    public TokenResponse loginGoogle(GoogleLoginRequest req) {
        GoogleTokenVerifier.GoogleUser g = google.verificar(req.idToken());
        return emitir(encontrarOuCriarPorGoogle(g.id(), g.email(), g.nome()));
    }

    /**
     * Acha o usuário pela conta Google (ou pelo e-mail, vinculando), ou cria um novo.
     * Usado pelo app (ID token) e pela página de login do conector do Claude.
     */
    @Transactional
    public Usuario encontrarOuCriarPorGoogle(String googleId, String emailBruto, String nome) {
        String email = normalizar(emailBruto);
        Usuario u = repo.findByGoogleId(googleId)
                .or(() -> repo.findByEmail(email))
                .orElseGet(() -> {
                    Usuario novo = new Usuario();
                    novo.setEmail(email);
                    novo.setNome(nome == null || nome.isBlank() ? email : nome);
                    return novo;
                });
        if (u.getGoogleId() == null) {
            u.setGoogleId(googleId);
        }
        return repo.save(u);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse eu(Long usuarioId) {
        return repo.findById(usuarioId)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));
    }

    private TokenResponse emitir(Usuario u) {
        TokenService.Token t = tokens.gerar(u);
        return new TokenResponse(t.valor(), "Bearer", t.expiraEm(), UsuarioResponse.from(u));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
