package com.apkfinancas.financas_api.security.oauth;

import com.apkfinancas.financas_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Login com e-mail e senha na página web do conector (mesmas contas do app). */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return repo.findByEmail(email.trim().toLowerCase())
                .map(u -> new UsuarioPrincipal(u.getId(), u.getEmail(), u.getSenhaHash()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
