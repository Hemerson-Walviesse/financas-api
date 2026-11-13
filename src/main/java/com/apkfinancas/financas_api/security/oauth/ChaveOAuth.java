package com.apkfinancas.financas_api.security.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

/**
 * Chave RSA que assina os tokens entregues ao Claude.
 * Fica guardada no banco: se fosse gerada a cada inicialização, todo "acordar"
 * do Render invalidaria os tokens e o usuário teria que reconectar o Claude.
 */
@Component
public class ChaveOAuth {

    private static final String ID = "principal";

    private final RSAKey chave;

    public ChaveOAuth(JdbcTemplate jdbc) {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS oauth_chave (
                    id VARCHAR(50) PRIMARY KEY,
                    jwk TEXT NOT NULL,
                    criada_em TIMESTAMP NOT NULL DEFAULT now()
                )""");
        List<String> salvas = jdbc.queryForList("SELECT jwk FROM oauth_chave WHERE id = ?", String.class, ID);
        if (salvas.isEmpty()) {
            jdbc.update("INSERT INTO oauth_chave (id, jwk) VALUES (?, ?) ON CONFLICT (id) DO NOTHING", ID, gerar());
            salvas = jdbc.queryForList("SELECT jwk FROM oauth_chave WHERE id = ?", String.class, ID);
        }
        try {
            this.chave = RSAKey.parse(salvas.get(0));
        } catch (ParseException e) {
            throw new IllegalStateException("Chave OAuth corrompida no banco (tabela oauth_chave)", e);
        }
    }

    public RSAKey rsa() {
        return chave;
    }

    private static String gerar() {
        try {
            return new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate().toJSONString();
        } catch (JOSEException e) {
            throw new IllegalStateException("Falha ao gerar a chave OAuth", e);
        }
    }
}
