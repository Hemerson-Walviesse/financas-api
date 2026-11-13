package com.apkfinancas.financas_api.mcp;

import com.apkfinancas.financas_api.dto.CategoriaResponse;
import com.apkfinancas.financas_api.dto.ContaRequest;
import com.apkfinancas.financas_api.dto.ContaResponse;
import com.apkfinancas.financas_api.dto.GastoRequest;
import com.apkfinancas.financas_api.dto.GastoResponse;
import com.apkfinancas.financas_api.dto.ResumoMensalResponse;
import com.apkfinancas.financas_api.service.CategoriaService;
import com.apkfinancas.financas_api.service.ContaService;
import com.apkfinancas.financas_api.service.GastoService;
import com.apkfinancas.financas_api.service.ResumoService;
import com.apkfinancas.financas_api.util.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Ferramentas que o Claude do usuário pode chamar. Cada uma reaproveita os services
 * da API REST, então as regras (validação, dono dos dados, recorrência) são as mesmas.
 */
@Component
@RequiredArgsConstructor
public class FinancasTools {

    private final ResumoService resumo;
    private final GastoService gastos;
    private final ContaService contas;
    private final CategoriaService categorias;

    @McpTool(name = "resumo_do_mes",
            description = "Resumo financeiro de um mês: total gasto, gastos por categoria, contas em aberto e as que vencem nos próximos 7 dias.")
    public ResumoMensalResponse resumoDoMes(
            @McpToolParam(description = "Mês no formato AAAA-MM. Se omitido, usa o mês atual.", required = false) String mes) {
        return resumo.resumo(UsuarioAtual.id(), mes(mes));
    }

    @McpTool(name = "listar_gastos", description = "Lista os gastos lançados em um mês, em ordem de data.")
    public List<GastoResponse> listarGastos(
            @McpToolParam(description = "Mês no formato AAAA-MM. Se omitido, usa o mês atual.", required = false) String mes) {
        return gastos.listar(UsuarioAtual.id(), mes(mes));
    }

    @McpTool(name = "adicionar_gasto",
            description = "Lança um gasto (dinheiro que já saiu). Se a categoria não existir, ela é criada.")
    public GastoResponse adicionarGasto(
            @McpToolParam(description = "Descrição curta, ex.: 'Mercado', 'Uber'", required = true) String descricao,
            @McpToolParam(description = "Valor em reais, positivo, ex.: 45.90", required = true) BigDecimal valor,
            @McpToolParam(description = "Data no formato AAAA-MM-DD. Se omitida, usa hoje.", required = false) String data,
            @McpToolParam(description = "Nome da categoria, ex.: 'Mercado'", required = false) String categoria) {
        Long uid = UsuarioAtual.id();
        Long categoriaId = categorias.buscarOuCriarPorNome(uid, categoria);
        return gastos.criar(uid, new GastoRequest(descricao, valor, data(data), categoriaId));
    }

    @McpTool(name = "listar_contas", description = "Lista as contas a pagar do usuário.")
    public List<ContaResponse> listarContas(
            @McpToolParam(description = "true para listar só as que ainda não foram pagas", required = false) Boolean somenteAbertas) {
        return contas.listar(UsuarioAtual.id(), Boolean.TRUE.equals(somenteAbertas));
    }

    @McpTool(name = "contas_a_vencer",
            description = "Contas em aberto que vencem até daqui a N dias, incluindo as atrasadas.")
    public List<ContaResponse> contasAVencer(
            @McpToolParam(description = "Quantidade de dias à frente. Padrão: 7.", required = false) Integer dias) {
        return contas.proximas(UsuarioAtual.id(), dias == null ? 7 : dias);
    }

    @McpTool(name = "adicionar_conta",
            description = "Cadastra uma conta a pagar com vencimento. Contas recorrentes geram a do mês seguinte quando pagas.")
    public ContaResponse adicionarConta(
            @McpToolParam(description = "Descrição, ex.: 'Conta de luz'", required = true) String descricao,
            @McpToolParam(description = "Valor em reais, positivo", required = true) BigDecimal valor,
            @McpToolParam(description = "Vencimento no formato AAAA-MM-DD", required = true) String vencimento,
            @McpToolParam(description = "true se a conta se repete todo mês", required = false) Boolean recorrente,
            @McpToolParam(description = "Nome da categoria, ex.: 'Casa'", required = false) String categoria) {
        Long uid = UsuarioAtual.id();
        Long categoriaId = categorias.buscarOuCriarPorNome(uid, categoria);
        return contas.criar(uid, new ContaRequest(descricao, valor, LocalDate.parse(vencimento),
                Boolean.TRUE.equals(recorrente), categoriaId));
    }

    @McpTool(name = "pagar_conta",
            description = "Marca uma conta como paga hoje. Use o id retornado por listar_contas ou contas_a_vencer.")
    public ContaResponse pagarConta(
            @McpToolParam(description = "Id da conta", required = true) Long contaId) {
        return contas.pagar(UsuarioAtual.id(), contaId);
    }

    @McpTool(name = "listar_categorias", description = "Lista as categorias de gastos e contas do usuário.")
    public List<CategoriaResponse> listarCategorias() {
        return categorias.listar(UsuarioAtual.id());
    }

    private static YearMonth mes(String mes) {
        return (mes == null || mes.isBlank()) ? Datas.mesAtual() : YearMonth.parse(mes.trim());
    }

    private static LocalDate data(String data) {
        return (data == null || data.isBlank()) ? Datas.hoje() : LocalDate.parse(data.trim());
    }
}
