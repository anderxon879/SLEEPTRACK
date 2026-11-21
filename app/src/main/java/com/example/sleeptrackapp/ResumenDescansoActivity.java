package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// PANTALLA: Dashboard principal. Muestra las horas dormidas, cumplimiento de meta y botones de acción rápida.
public class ResumenDescansoActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar los componentes de texto donde se mostrarán las métricas de sueño
    TextView tvTitulo, tvFecha, tvDuracionEstimada, tvCumplimiento;
    TextView tvInicioVal, tvDespertarVal, tvEficienciaVal;
    TextView tvCalidadVal, tvInterrupcionesVal, tvInteraccionesVal, tvMetaEst;
    // ESTO SIRVE PARA: Declarar los botones para iniciar monitoreo nocturno, encuesta o ver gráficos
    Button btnIniciarMonitoreo, btnRegistrarComoDormi, btnVerGraficos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Cargar el layout XML de la pantalla principal
        setContentView(R.layout.activity_hu12_hu13_hu14_resumen_descanso);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar cada vista del XML con su variable correspondiente en Java
        tvTitulo = findViewById(R.id.tv_hu10_titulo);
        tvFecha = findViewById(R.id.tv_hu10_fecha);
        tvDuracionEstimada = findViewById(R.id.tv_hu10_duracion_estimada);
        tvCumplimiento = findViewById(R.id.tv_hu10_cumplimiento);
        tvInicioVal = findViewById(R.id.tv_hu10_inicio_val);
        tvDespertarVal = findViewById(R.id.tv_hu10_despertar_val);
        tvEficienciaVal = findViewById(R.id.tv_hu10_eficiencia_val);
        tvCalidadVal = findViewById(R.id.tv_hu10_calidad_val);
        tvInterrupcionesVal = findViewById(R.id.tv_hu10_interrupciones_val);
        tvInteraccionesVal = findViewById(R.id.tv_hu10_interacciones_val);
        tvMetaEst = findViewById(R.id.tv_hu10_meta_est);

        btnIniciarMonitoreo = findViewById(R.id.btn_iniciar_monitoreo);
        btnRegistrarComoDormi = findViewById(R.id.btn_registrar_como_dormi);
        btnVerGraficos = findViewById(R.id.btn_ver_graficos);

        // ESTO SIRVE PARA: Abrir la pantalla de monitoreo nocturno con acelerómetro
        if (btnIniciarMonitoreo != null) {
            btnIniciarMonitoreo.setOnClickListener(v -> startActivity(new Intent(this, MonitoreoActivity.class)));
        }

        // ESTO SIRVE PARA: Abrir la encuesta matutina (estrés, pantallas, cafeína)
        if (btnRegistrarComoDormi != null) {
            btnRegistrarComoDormi.setOnClickListener(v -> startActivity(new Intent(this, RegistroDiarioActivity.class)));
        }

        // ESTO SIRVE PARA: Abrir la pantalla de gráficos y estadísticas semanales
        if (btnVerGraficos != null) {
            btnVerGraficos.setOnClickListener(v -> startActivity(new Intent(this, AnalisisSemanalActivity.class)));
        }

        // ESTO SIRVE PARA: Permitir deslizar hacia abajo para refrescar los datos del dashboard
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipe_refresh_resumen);
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent_mint);
            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.card_surface);
            swipeRefresh.setOnRefreshListener(() -> {
                cargarResumenData();
                swipeRefresh.setRefreshing(false);
            });
        }

        // ESTO SIRVE PARA: Cargar las métricas de descanso del estudiante al iniciar
        cargarResumenData();

        // ESTO SIRVE PARA: Activar la barra de navegación inferior (Home activo)
        BottomNavHelper.setup(this, R.id.nav_inicio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ESTO SIRVE PARA: Refrescar automáticamente los datos si el usuario vuelve de monitorear o registrar
        cargarResumenData();
    }

    // ESTO SIRVE PARA: Leer datos de SharedPreferences y consultar a api/obtener_resumen.php
    private void cargarResumenData() {
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correo = prefs.getString("correo_usuario", "");

        // ESTO SIRVE PARA: Obtener los valores locales guardados por la última sesión de monitoreo
        long inicioMillis = prefs.getLong("ultimo_monitoreo_inicio", 0);
        long finMillis = prefs.getLong("ultimo_monitoreo_fin", 0);
        int movimientos = prefs.getInt("movimientos_detectados", 3);
        int targetMinutos = prefs.getInt("duracion_minutos_totales", 480);
        String metaHorasTexto = prefs.getString("meta_horas", "8 horas");
        int interaccionesPantalla = prefs.getInt("interacciones_pantalla", 0);

        if (tvMetaEst != null) {
            tvMetaEst.setText("Meta: " + metaHorasTexto);
        }

        // ESTO SIRVE PARA: Mostrar indicador visual del uso de pantallas durante la madrugada
        if (tvInteraccionesVal != null) {
            if (interaccionesPantalla == 0) {
                tvInteraccionesVal.setText("0 eventos (Excelente)");
                tvInteraccionesVal.setTextColor(getColor(R.color.accent_mint));
            } else if (interaccionesPantalla <= 2) {
                tvInteraccionesVal.setText(interaccionesPantalla + (interaccionesPantalla == 1 ? " evento" : " eventos"));
                tvInteraccionesVal.setTextColor(getColor(R.color.warning));
            } else {
                tvInteraccionesVal.setText(interaccionesPantalla + " eventos (Uso alto)");
                tvInteraccionesVal.setTextColor(getColor(R.color.error));
            }
        }

        // ESTO SIRVE PARA: Calcular duración real y porcentajes si hay una sesión de monitoreo completada
        if (inicioMillis > 0 && finMillis > inicioMillis) {
            long duracionMillis = finMillis - inicioMillis;
            int duracionMinutosReales = (int) (duracionMillis / (1000 * 60));
            if (duracionMinutosReales <= 0 && duracionMillis >= 25000) {
                duracionMinutosReales = 1;
            } else if (duracionMinutosReales < 0) {
                duracionMinutosReales = 0;
            }

            int horas = duracionMinutosReales / 60;
            int mins = duracionMinutosReales % 60;
            String durTexto = SleepTimeUtils.formatearDuracionCorto(horas, mins);

            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String horaInicio = sdfHora.format(new Date(inicioMillis));
            String horaFin = sdfHora.format(new Date(finMillis));

            // ESTO SIRVE PARA: Aplicar algoritmos matemáticos de eficiencia y calidad de descanso
            int calidadPorcentaje = SleepTimeUtils.calcularCalidadPorcentaje(duracionMinutosReales, targetMinutos, movimientos);
            int eficienciaPorcentaje = SleepTimeUtils.calcularEficiencia(duracionMinutosReales, movimientos);
            String clasificacionCalidad = SleepTimeUtils.clasificarCalidad(calidadPorcentaje);

            if (tvInicioVal != null) tvInicioVal.setText(horaInicio);
            if (tvDespertarVal != null) tvDespertarVal.setText(horaFin);
            if (tvDuracionEstimada != null) tvDuracionEstimada.setText(durTexto);
            if (tvCumplimiento != null) tvCumplimiento.setText(calidadPorcentaje + "%");
            if (tvEficienciaVal != null) tvEficienciaVal.setText(eficienciaPorcentaje + "%");
            if (tvCalidadVal != null) tvCalidadVal.setText(getEmojiForCalidad(clasificacionCalidad) + " " + clasificacionCalidad);
        } else {
            // Valores por defecto si aún no ha registrado noches de sueño
            String hDormir = prefs.getString("hora_dormir", "23:00");
            String hDespertar = prefs.getString("hora_despertar", "07:00");

            if (tvInicioVal != null) tvInicioVal.setText(hDormir);
            if (tvDespertarVal != null) tvDespertarVal.setText(hDespertar);
            if (tvDuracionEstimada != null) tvDuracionEstimada.setText("0h 00m");
            if (tvCumplimiento != null) tvCumplimiento.setText("0%");
            if (tvEficienciaVal != null) tvEficienciaVal.setText("0%");
            if (tvCalidadVal != null) tvCalidadVal.setText("Sin registro");
        }

        if (correo.isEmpty()) return;

        // ESTO SIRVE PARA: Consultar la base de datos MySQL mediante la API en PHP usando Volley
        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_resumen.php";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ESTO SIRVE PARA: Procesar la respuesta del servidor y sincronizar los números del dashboard
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success") && response.has("data")) {
                            JSONObject data = response.getJSONObject("data");

                            // Actualiza duración, calidad y horas desde el registro de la base de datos
                            if (data.has("duracion_minutos")) {
                                int durMin = data.optInt("duracion_minutos", 0);
                                int h = durMin / 60;
                                int m = durMin % 60;
                                if (tvDuracionEstimada != null) tvDuracionEstimada.setText(h + "h " + String.format(Locale.getDefault(), "%02dm", m));
                            }

                            if (data.has("calidad_porcentaje") && tvCumplimiento != null) {
                                int calPct = data.optInt("calidad_porcentaje", 0);
                                tvCumplimiento.setText(calPct + "%");
                            }

                            if (data.has("eficiencia") && tvEficienciaVal != null) {
                                int efPct = data.optInt("eficiencia", 0);
                                tvEficienciaVal.setText(efPct + "%");
                            }

                            String hIni = data.optString("hora_inicio", "");
                            if (!hIni.isEmpty() && tvInicioVal != null) {
                                tvInicioVal.setText(hIni);
                            }

                            String hFin = data.optString("hora_fin", "");
                            if (!hFin.isEmpty() && tvDespertarVal != null) {
                                tvDespertarVal.setText(hFin);
                            }

                            if (data.has("calidad") && tvCalidadVal != null) {
                                String cal = data.getString("calidad");
                                tvCalidadVal.setText(getEmojiForCalidad(cal) + " " + cal);
                            }

                            if (data.has("interrupciones") && tvInterrupcionesVal != null) {
                                tvInterrupcionesVal.setText(data.getString("interrupciones"));
                            }

                            String pan = data.optString("pantallas", "");
                            if (!pan.isEmpty() && tvInteraccionesVal != null) {
                                tvInteraccionesVal.setText(pan);
                                if (pan.contains("0") || pan.contains("Menos")) {
                                    tvInteraccionesVal.setTextColor(getColor(R.color.accent_mint));
                                } else if (pan.contains("Más") || pan.contains("alto")) {
                                    tvInteraccionesVal.setTextColor(getColor(R.color.error));
                                }
                            }

                            if (data.has("fecha") && tvFecha != null) {
                                tvFecha.setText("Noche del " + data.getString("fecha"));
                            }

                            if (data.has("target_meta_horas") && tvMetaEst != null) {
                                tvMetaEst.setText("Meta: " + data.getString("target_meta_horas"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private String getEmojiForCalidad(String calidad) {
        if (calidad == null) return "😐";
        switch (calidad) {
            case "Muy mala": return "😫";
            case "Mala": return "🙁";
            case "Buena": return "🙂";
            case "Muy buena": return "🤩";
            default: return "😐";
        }
    }
}
