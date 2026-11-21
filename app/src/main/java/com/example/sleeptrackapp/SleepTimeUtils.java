package com.example.sleeptrackapp;

import java.util.Locale;

// UTILIDAD: Funciones matemáticas para calcular horas entre medianoche, calidad % y formatos de tiempo.
public class SleepTimeUtils {

    // ESTO SIRVE PARA: Calcular horas y minutos entre hora de dormir y despertar, resolviendo el cruce de medianoche (+24h)
    public static int[] calcularHorasYMinutos(String horaDormirStr, String horaDespertarStr) {
        int h1 = 23, m1 = 0;
        int h2 = 7, m2 = 0;

        try {
            if (horaDormirStr != null) {
                String[] p1 = horaDormirStr.split(":");
                if (p1.length == 2) {
                    h1 = Integer.parseInt(p1[0].trim());
                    m1 = Integer.parseInt(p1[1].trim());
                }
            }
        } catch (Exception ignored) {}

        try {
            if (horaDespertarStr != null) {
                String[] p2 = horaDespertarStr.split(":");
                if (p2.length == 2) {
                    h2 = Integer.parseInt(p2[0].trim());
                    m2 = Integer.parseInt(p2[1].trim());
                }
            }
        } catch (Exception ignored) {}

        // ESTO SIRVE PARA: Convertir ambas horas a minutos del día
        int min1 = h1 * 60 + m1;
        int min2 = h2 * 60 + m2;
        int diff = min2 - min1;

        // GRACIAS A ESTO: Si la resta da negativo (ej. durmió a las 23:00 y despertó a las 07:00), suma 1440 min (24 horas)
        if (diff <= 0) {
            diff += 24 * 60;
        }

        int horas = diff / 60;
        int minutos = diff % 60;
        return new int[]{horas, minutos};
    }

    // ESTO SIRVE PARA: Calcular el porcentaje de calidad combinando horas dormidas vs meta y penalizando por movimientos
    public static int calcularCalidadPorcentaje(int duracionMinutosReales, int duracionMinutosMeta, int movimientosAcelerometro) {
        if (duracionMinutosReales <= 1) return 0;
        if (duracionMinutosMeta <= 0) duracionMinutosMeta = 480;

        // Cumplimiento respecto a la meta (máximo 100%)
        float ratioTiempo = (float) duracionMinutosReales / duracionMinutosMeta;
        if (ratioTiempo > 1.0f) ratioTiempo = 1.0f;
        float baseCalidad = ratioTiempo * 100.0f;

        // Cada movimiento brusco detectado por el acelerómetro resta 2% de calidad
        float penalizacionMovimiento = movimientosAcelerometro * 2.0f;

        int resultado = Math.round(baseCalidad - penalizacionMovimiento);
        if (resultado > 100) resultado = 100;
        if (resultado < 0) resultado = 0;

        return resultado;
    }

    // ESTO SIRVE PARA: Traducir el porcentaje numérico en una etiqueta descriptiva
    public static String clasificarCalidad(int porcentaje) {
        if (porcentaje >= 85) return "Muy buena";
        if (porcentaje >= 70) return "Buena";
        if (porcentaje >= 50) return "Regular";
        if (porcentaje >= 25) return "Mala";
        return "Muy mala";
    }

    // ESTO SIRVE PARA: Calcular la eficiencia porcentual del descanso
    public static int calcularEficiencia(int duracionMinutosReales, int movimientosAcelerometro) {
        if (duracionMinutosReales <= 1) return 0;
        int base;
        if (duracionMinutosReales < 30) {
            base = Math.round((duracionMinutosReales / 30.0f) * 70.0f);
        } else {
            base = 95;
        }
        int eficiencia = base - (movimientosAcelerometro * 2);
        if (eficiencia > 99) eficiencia = 99;
        if (eficiencia < 0) eficiencia = 0;
        return eficiencia;
    }

    // ESTO SIRVE PARA: Dar formato descriptivo en texto largo para metas (ej. "8 horas de descanso" o "7 horas 30 min de descanso")
    public static String formatearDuracionTexto(int horas, int minutos) {
        if (minutos == 0) {
            return horas == 1 ? "1 hora de descanso" : horas + " horas de descanso";
        } else {
            String horasStr = (horas == 1) ? "1 hora" : horas + " horas";
            return horasStr + " " + minutos + " min de descanso";
        }
    }

    // ESTO SIRVE PARA: Dar formato compacto para tarjetas y gráficos (ej. "7h 15m")
    public static String formatearDuracionCorto(int horas, int minutos) {
        return String.format(Locale.getDefault(), "%dh %02dm", horas, minutos);
    }
}
