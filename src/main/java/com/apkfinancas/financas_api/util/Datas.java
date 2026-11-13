package com.apkfinancas.financas_api.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

public final class Datas {
    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private Datas() {}

    public static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }

    public static YearMonth mesAtual() {
        return YearMonth.now(ZONA);
    }
}
