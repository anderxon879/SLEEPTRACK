package com.example.sleeptrackapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Locale;

// PANTALLA: Configuración de descanso. Define hora de dormir, despertar y meta de horas diarias (sincroniza con api/configurar.php).
public class ConfigurarDescansoActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar botones, textos de horas objetivo y switches de recordatorio
    View ivVolver;
    TextView tvHoraDormirVal, tvHoraDespertarVal, tvMetaVal;
    MaterialSwitch switchRecordatorio, switchGuardarLocal;
    Button btnGuardarConfiguracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Cargar el layout XML de configuración de horas y metas de descanso
        setContentView(R.layout.activity_hu06_configurar_descanso);

        // ESTO SIRVE PARA: Ajustar márgenes para evitar solapamientos con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar los controles visuales del layout XML con los objetos Java
        ivVolver = findViewById(R.id.iv_hu04_volver);
        tvHoraDormirVal = findViewById(R.id.tv_hu04_hora_dormir_val);
        tvHoraDespertarVal = findViewById(R.id.tv_hu04_hora_despertar_val);
        tvMetaVal = findViewById(R.id.tv_hu04_meta_val);
        switchRecordatorio = findViewById(R.id.switch_hu04_recordatorio);
        switchGuardarLocal = findViewById(R.id.switch_hu04_guardar_local);
        btnGuardarConfiguracion = findViewById(R.id.btn_hu04_guardar_configuracion);

        // ESTO SIRVE PARA: Regresar a la pantalla anterior o al inicio al presionar la flecha
        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> salirConfiguracion());
        }

        // ESTO SIRVE PARA: Manejar el botón físico de retroceso del teléfono
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                salirConfiguracion();
            }
        });

        // ESTO SIRVE PARA: Cargar los horarios previamente guardados en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String savedHoraDormir = prefs.getString("hora_dormir", "23:00");
        String savedHoraDespertar = prefs.getString("hora_despertar", "07:00");
        if (tvHoraDormirVal != null) tvHoraDormirVal.setText(savedHoraDormir);
        if (tvHoraDespertarVal != null) tvHoraDespertarVal.setText(savedHoraDespertar);

        // ESTO SIRVE PARA: Calcular automáticamente las horas y minutos resultantes
        actualizarCalculoDescanso();

        // ESTO SIRVE PARA: Abrir el selector interactivo TimePickerDialog para la hora de acostarse
        if (tvHoraDormirVal != null) {
            tvHoraDormirVal.setOnClickListener(v -> mostrarSelectorHora(tvHoraDormirVal, true));
            if (tvHoraDormirVal.getParent() instanceof View) {
                ((View) tvHoraDormirVal.getParent()).setOnClickListener(v -> mostrarSelectorHora(tvHoraDormirVal, true));
            }
        }
        // ESTO SIRVE PARA: Abrir el selector interactivo TimePickerDialog para la hora de levantarse
        if (tvHoraDespertarVal != null) {
            tvHoraDespertarVal.setOnClickListener(v -> mostrarSelectorHora(tvHoraDespertarVal, false));
            if (tvHoraDespertarVal.getParent() instanceof View) {
                ((View) tvHoraDespertarVal.getParent()).setOnClickListener(v -> mostrarSelectorHora(tvHoraDespertarVal, false));
            }
        }

        // ESTO SIRVE PARA: Guardar la configuración en MySQL mediante Volley hacia api/configurar.php
        if (btnGuardarConfiguracion != null) {
            btnGuardarConfiguracion.setOnClickListener(v -> {
                String hDormir = tvHoraDormirVal != null ? tvHoraDormirVal.getText().toString() : "23:00";
                String hDespertar = tvHoraDespertarVal != null ? tvHoraDespertarVal.getText().toString() : "07:00";

                // ESTO SIRVE PARA: Calcular la duración entre horas con el algoritmo que resuelve la medianoche
                int[] dur = SleepTimeUtils.calcularHorasYMinutos(hDormir, hDespertar);
                String durTexto = SleepTimeUtils.formatearDuracionTexto(dur[0], dur[1]);
                String durCorto = SleepTimeUtils.formatearDuracionCorto(dur[0], dur[1]);

                String correo = prefs.getString("correo_usuario", "");
                int sensoresAceptados = prefs.getBoolean("sensores_aceptados", false) ? 1 : 0;

                // ESTO SIRVE PARA: URL del endpoint PHP en XAMPP que actualiza la configuración en la base de datos
                String url = "http://192.168.3.225/SLEEPTRACKAPP/api/configurar.php";

                // ESTO SIRVE PARA: Empaquetar la configuración horaria del estudiante en JSON
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("correo", correo);
                    requestData.put("hora_dormir", hDormir);
                    requestData.put("hora_despertar", hDespertar);
                    requestData.put("meta_horas", durTexto);
                    requestData.put("sensores_aceptados", sensoresAceptados);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnGuardarConfiguracion.setEnabled(false);

                // ESTO SIRVE PARA: Enviar la petición HTTP POST asíncrona mediante la librería Volley
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                        response -> {
                            btnGuardarConfiguracion.setEnabled(true);
                            try {
                                String status = response.getString("status");
                                String message = response.getString("message");

                                // GRACIAS A ESTO: Si el guardado es exitoso, actualiza el caché de SharedPreferences en el móvil
                                if (status.equals("success")) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("hora_dormir", hDormir);
                                    editor.putString("hora_despertar", hDespertar);
                                    editor.putString("meta_horas", durTexto);
                                    editor.putString("duracion_calculada_texto", durTexto);
                                    editor.putString("duracion_calculada_corto", durCorto);
                                    editor.putInt("duracion_minutos_totales", dur[0] * 60 + dur[1]);
                                    if (switchRecordatorio != null) editor.putBoolean("recordatorios_activos", switchRecordatorio.isChecked());
                                    editor.apply();

                                    CustomToast.show(ConfigurarDescansoActivity.this, "Configuración guardada correctamente", false);

                                    // ESTO SIRVE PARA: Si vino desde la pantalla Perfil, solo cierra; si es primer registro, va al Dashboard
                                    boolean desdePerfil = getIntent().getBooleanExtra("desde_perfil", false);
                                    if (desdePerfil) {
                                        finish();
                                    } else {
                                        Intent intent = new Intent(ConfigurarDescansoActivity.this, ResumenDescansoActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    CustomToast.show(ConfigurarDescansoActivity.this, message, true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            // En caso de estar sin conexión, persiste la configuración en la memoria local del dispositivo
                            btnGuardarConfiguracion.setEnabled(true);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("hora_dormir", hDormir);
                            editor.putString("hora_despertar", hDespertar);
                            editor.putString("meta_horas", durTexto);
                            editor.putString("duracion_calculada_texto", durTexto);
                            editor.putString("duracion_calculada_corto", durCorto);
                            editor.putInt("duracion_minutos_totales", dur[0] * 60 + dur[1]);
                            editor.apply();

                            CustomToast.show(ConfigurarDescansoActivity.this, "Horario guardado en tu dispositivo", false);
                            boolean desdePerfil = getIntent().getBooleanExtra("desde_perfil", false);
                            if (desdePerfil) {
                                finish();
                            } else {
                                Intent intent = new Intent(ConfigurarDescansoActivity.this, ResumenDescansoActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                );

                Volley.newRequestQueue(this).add(jsonObjectRequest);
            });
        }
    }

    // ESTO SIRVE PARA: Recalcular en tiempo real el tiempo de descanso estimado entre hora de dormir y despertar
    private void actualizarCalculoDescanso() {
        String hDormir = tvHoraDormirVal != null ? tvHoraDormirVal.getText().toString() : "23:00";
        String hDespertar = tvHoraDespertarVal != null ? tvHoraDespertarVal.getText().toString() : "07:00";

        int[] dur = SleepTimeUtils.calcularHorasYMinutos(hDormir, hDespertar);
        String texto = SleepTimeUtils.formatearDuracionTexto(dur[0], dur[1]);

        if (tvMetaVal != null) {
            tvMetaVal.setText(texto);
        }
    }

    // ESTO SIRVE PARA: Desplegar el selector de hora nativo (reloj interactivo) TimePickerDialog
    private void mostrarSelectorHora(TextView targetView, boolean esHoraDormir) {
        String currentText = targetView.getText().toString();
        int horaDefecto = esHoraDormir ? 23 : 7;
        int minutoDefecto = 0;

        try {
            String[] parts = currentText.split(":");
            if (parts.length == 2) {
                horaDefecto = Integer.parseInt(parts[0].trim());
                minutoDefecto = Integer.parseInt(parts[1].trim());
            }
        } catch (Exception ignored) {}

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    targetView.setText(timeStr);
                    actualizarCalculoDescanso();
                },
                horaDefecto,
                minutoDefecto,
                true
        );

        timePickerDialog.show();
    }

    // ESTO SIRVE PARA: Controlar la navegación de salida según si el usuario proviene de Perfil o del registro inicial
    private void salirConfiguracion() {
        boolean desdePerfil = getIntent().getBooleanExtra("desde_perfil", false);
        if (desdePerfil) {
            finish();
        } else {
            Intent intent = new Intent(this, ResumenDescansoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
