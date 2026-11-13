package com.apkfinancas.financas_api.controller;

import com.apkfinancas.financas_api.security.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Página que o usuário vê quando conecta o app ao Claude.
 * Visual com as cores do Adão (marca eva), sem depender de template engine.
 */
@Controller
public class LoginPageController {

    private final SecurityConfig seguranca;

    public LoginPageController(SecurityConfig seguranca) {
        this.seguranca = seguranca;
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String login(HttpServletRequest req) {
        if (req.getParameter("conectado") != null) {
            return pagina("""
                    <div class="selo ok">✓</div>
                    <h1>Tudo certo!</h1>
                    <p class="sub">Sua conta está conectada. Pode fechar esta aba e voltar para o Claude.</p>
                    """);
        }

        String aviso = "";
        if (req.getParameter("erro") != null) {
            aviso = "<div class=\"alerta\">E-mail ou senha inválidos.</div>";
        } else if (req.getParameter("erro-google") != null) {
            aviso = "<div class=\"alerta\">Não foi possível entrar com o Google. Tente de novo.</div>";
        }

        CsrfToken csrf = (CsrfToken) req.getAttribute("_csrf");
        String campoCsrf = csrf == null ? ""
                : "<input type=\"hidden\" name=\"" + csrf.getParameterName() + "\" value=\"" + csrf.getToken() + "\">";

        String google = seguranca.googleWebAtivo()
                ? """
                  <div class="ou"><span>ou</span></div>
                  <a class="botao contorno" href="/oauth2/authorization/google">Entrar com Google</a>
                  """
                : "";

        return pagina("""
                <div class="selo">R$</div>
                <h1>Conectar ao Claude</h1>
                <p class="sub">Entre com a mesma conta do app Finanças. O Claude poderá consultar e lançar seus gastos e contas.</p>
                <div class="card">
                  %s
                  <form method="post" action="/login">
                    %s
                    <label>E-mail<input type="email" name="email" autocomplete="email" required autofocus></label>
                    <label>Senha<input type="password" name="senha" autocomplete="current-password" required></label>
                    <button class="botao" type="submit">Entrar e conectar</button>
                  </form>
                  %s
                </div>
                <p class="rodape">Você pode desconectar a qualquer momento nas configurações de conectores do Claude.</p>
                """.formatted(aviso, campoCsrf, google));
    }

    private static String pagina(String conteudo) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Finanças · Conectar ao Claude</title>
                <style>
                  :root { --primaria:#1A7E85; --primaria-escura:#0E6970; --fundo:#FAFBF8; --superficie:#FFFFFF;
                          --texto:#1C1B1A; --suave:#4A4946; --borda:#ECECEA; --erro:#C4141F; --erro-fundo:#FBE8E8; }
                  @media (prefers-color-scheme: dark) {
                    :root { --fundo:#0A0A09; --superficie:#121110; --texto:#FAFAFA; --suave:#DCDCDB; --borda:#2A2928;
                            --primaria:#06636A; --primaria-escura:#1A7E85; --erro-fundo:#3A1416; --erro:#FF8A8F; }
                  }
                  * { box-sizing:border-box; }
                  body { margin:0; min-height:100vh; display:flex; align-items:center; justify-content:center;
                         background:var(--fundo); color:var(--texto);
                         font-family:system-ui,-apple-system,"Segoe UI",Roboto,sans-serif; padding:24px; }
                  main { width:100%%; max-width:400px; }
                  .selo { width:56px; height:56px; border-radius:14px; background:var(--primaria); color:#fff;
                          display:flex; align-items:center; justify-content:center; font-weight:600; font-size:20px; }
                  .selo.ok { font-size:28px; }
                  h1 { font-size:30px; font-weight:600; margin:20px 0 4px; }
                  .sub { color:var(--suave); font-size:16px; line-height:1.5; margin:0 0 24px; }
                  .card { background:var(--superficie); border:1px solid var(--borda); border-radius:14px; padding:20px; }
                  label { display:block; font-size:14px; font-weight:500; margin-bottom:12px; }
                  input { display:block; width:100%%; margin-top:6px; padding:12px; font-size:16px; color:var(--texto);
                          background:var(--superficie); border:1px solid #B5B4B1; border-radius:6px; }
                  input:focus { outline:2px solid var(--primaria); outline-offset:-1px; border-color:var(--primaria); }
                  .botao { display:block; width:100%%; margin-top:8px; padding:14px; border:0; border-radius:6px;
                           background:var(--primaria); color:#fff; font-size:14px; font-weight:600;
                           text-align:center; text-decoration:none; cursor:pointer; }
                  .botao:hover { background:var(--primaria-escura); }
                  .botao.contorno { background:transparent; color:var(--texto); border:1px solid #D9D8D5; }
                  .ou { display:flex; align-items:center; gap:12px; margin:16px 0 8px; color:var(--suave); font-size:12px; }
                  .ou::before, .ou::after { content:""; flex:1; height:1px; background:var(--borda); }
                  .alerta { background:var(--erro-fundo); color:var(--erro); border-radius:6px; padding:12px;
                            font-size:14px; margin-bottom:16px; }
                  .rodape { color:var(--suave); font-size:12px; text-align:center; margin-top:16px; }
                </style>
                </head>
                <body><main>
                %s
                </main></body>
                </html>
                """.formatted(conteudo);
    }
}
