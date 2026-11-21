package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Locale;

// PANTALLA: Perfil del estudiante. Muestra datos académicos, noches acumuladas, promedios y opción de cierre de sesión.
public class PerfilCuentaActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar campos del estudiante (nombre, carrera, universidad) y métricas acumuladas
    View ivVolver;
    TextView tvNombre, tvCarrera, tvUniversidad;
    TextView tvNochesVal, tvPromedioVal, tvEficienciaVal;
    TextView tvCorreoVal, tvHorariosVal;
    // ESTO SIRVE PARA: Botones de acceso a configuración de horarios, privacidad y cerrar sesión
    View btnConfiguracion, btnPrivacidad, btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ESTO SIRVE PARA: Cargar el layout XML de la pantalla de perfil
        setContentView(R.layout.activity_hu21_hu22_perfil_cuenta);

        // ESTO SIRVE PARA: Vincular los textos y botones del XML con las variables en Java
        ivVolver = findViewById(R.id.iv_hu21_volver);
        tvNombre = findViewById(R.id.tv_hu21_nombre);
        tvCarrera = findViewById(R.id.tv_hu21_carrera);
        tvUniversidad = findViewById(R.id.tv_hu21_universidad);

        tvNochesVal = findViewById(R.id.tv_perfil_noches_val);
        tvPromedioVal = findViewById(R.id.tv_perfil_promedio_val);
        tvEficienciaVal = findViewById(R.id.tv_perfil_eficiencia_val);

        tvCorreoVal = findViewById(R.id.tv_perfil_correo_val);
        tvHorariosVal = findViewById(R.id.tv_perfil_horarios_val);

        btnConfiguracion = findViewById(R.id.btn_configurar_descanso);
        btnPrivacidad = findViewById(R.id.btn_privacidad);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);

        // ESTO SIRVE PARA: Regresar al Dashboard al tocar la flecha de volver
        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> volverAInicio());
        }

        // ESTO SIRVE PARA: Controlar el botón de retroceso del teléfono
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAInicio();
            }
        });

        // ESTO SIRVE PARA: Consultar al servidor api/obtener_perfil.php y traer las estadísticas históricas
        cargarDatosPerfil();

        // ESTO SIRVE PARA: Botón para modificar la meta u horarios de sueño
        if (btnConfiguracion != null) {
            btnConfiguracion.setOnClickListener(v -> {
                Intent intent = new Intent(this, ConfigurarDescansoActivity.class);
                intent.putExtra("desde_perfil", true);
                startActivity(intent);
            });
        }

        // ESTO SIRVE PARA: Botón para gestionar la privacidad de datos (borrado de historial o cuenta)
        if (btnPrivacidad != null) {
            btnPrivacidad.setOnClickListener(v -> startActivity(new Intent(this, PrivacidadDatosActivity.class)));
        }

        // ESTO SIRVE PARA: Botón para cerrar la sesión activa y limpiar SharedPreferences
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> mostrarDialogoCerrarSesion());
        }

        // ESTO SIRVE PARA: Configurar la barra de navegación inferior (pestaña Perfil activa)
        BottomNavHelper.setup(this, R.id.nav_perfil);
    }

    // ESTO SIRVE PARA: Refrescar los datos del perfil cuando el usuario regresa a esta pantalla
    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosPerfil();
    }

    // ESTO SIRVE PARA: Cargar datos locales de SharedPreferences y sincronizar con la base de datos MySQL
    private void cargarDatosPerfil() {
        // ESTO SIRVE PARA: Obtener los datos del estudiante guardados en la memoria caché del teléfono
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String nombre = prefs.getString("nombre_usuario", "Estudiante Universitario");
        String carrera = prefs.getString("carrera_usuario", "Ingeniería de Sistemas");
        String universidad = prefs.getString("universidad_usuario", "Universidad Nacional");
        String correo = prefs.getString("correo_usuario", "estudiante@universidad.edu.pe");

        String hDormir = prefs.getString("hora_dormir", "23:00");
        String hDespertar = prefs.getString("hora_despertar", "07:00");
        String metaHoras = prefs.getString("meta_horas", "8 horas");

        // ESTO SIRVE PARA: Asignar los valores leídos a las etiquetas correspondientes en pantalla
        if (tvNombre != null) tvNombre.setText(nombre);
        if (tvCarrera != null) tvCarrera.setText(carrera);
        if (tvUniversidad != null) tvUniversidad.setText(universidad);
        if (tvCorreoVal != null) tvCorreoVal.setText(correo);
        if (tvHorariosVal != null) tvHorariosVal.setText(hDormir + " - " + hDespertar + " (" + metaHoras + ")");

        // ESTO SIRVE PARA: Cargar estadísticas acumuladas en caché para visualización instantánea sin internet
        int totalNochesCache = prefs.getInt("total_noches_registradas", -1);
        String promTxtCache = prefs.getString("promedio_duracion_cache", "");
        int promEficienciaCache = prefs.getInt("promedio_eficiencia_cache", -1);

        if (totalNochesCache != -1) {
            if (tvNochesVal != null) tvNochesVal.setText(String.valueOf(totalNochesCache));
            if (tvPromedioVal != null && !promTxtCache.isEmpty()) tvPromedioVal.setText(promTxtCache);
            if (tvEficienciaVal != null && promEficienciaCache != -1) tvEficienciaVal.setText(promEficienciaCache + "%");
        } else {
            int durMin = prefs.getInt("ultima_duracion_minutos", 0);
            int efPct = prefs.getInt("ultima_eficiencia", 0);
            long ultFin = prefs.getLong("ultimo_monitoreo_fin", 0);
            int nochesInit = ultFin > 0 ? 1 : 0;

            if (tvNochesVal != null) tvNochesVal.setText(String.valueOf(nochesInit));
            if (tvPromedioVal != null) {
                int h = durMin / 60;
                int m = durMin % 60;
                tvPromedioVal.setText(h + "h " + String.format(Locale.getDefault(), "%02dm", m));
            }
            if (tvEficienciaVal != null) tvEficienciaVal.setText(efPct + "%");
        }

        // ESTO SIRVE PARA: Consultar el endpoint api/obtener_perfil.php mediante Volley para actualizar estadísticas desde MySQL
        if (!correo.isEmpty()) {
            String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_perfil.php";
            JSONObject payload = new JSONObject();
            try {
                payload.put("correo", correo);
            } catch (Exception ignored) {}

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                if (response.has("user")) {
                                    JSONObject userObj = response.getJSONObject("user");
                                    String n = userObj.optString("nombres", nombre);
                                    String c = userObj.optString("carrera", carrera);
                                    String u = userObj.optString("universidad", universidad);
                                    String hd = userObj.optString("hora_dormir", hDormir);
                                    String hdesp = userObj.optString("hora_despertar", hDespertar);
                                    String meta = userObj.optString("meta_horas", metaHoras);
                                    if (tvNombre != null) tvNombre.setText(n);
                                    if (tvCarrera != null) tvCarrera.setText(c);
                                    if (tvUniversidad != null) tvUniversidad.setText(u);
                                    if (tvHorariosVal != null) tvHorariosVal.setText(hd + " - " + hdesp + " (" + meta + ")");
                                }

                                if (response.has("stats")) {
                                    JSONObject stats = response.getJSONObject("stats");
                                    int tot = stats.optInt("total_noches", 0);
                                    String promDur = stats.optString("promedio_duracion_texto", "0h 00m");
                                    int promEf = stats.optInt("promedio_eficiencia", 0);

                                    if (tvNochesVal != null) tvNochesVal.setText(String.valueOf(tot));
                                    if (tvPromedioVal != null) tvPromedioVal.setText(promDur);
                                    if (tvEficienciaVal != null) tvEficienciaVal.setText(promEf + "%");

                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putInt("total_noches_registradas", tot);
                                    editor.putString("promedio_duracion_cache", promDur);
                                    editor.putInt("promedio_eficiencia_cache", promEf);
                                    editor.apply();
                                }
                            }
                        } catch (Exception ignored) {}
                    },
                    error -> {}
            );
            Volley.newRequestQueue(this).add(request);
        }
    }

    // ESTO SIRVE PARA: Mostrar el diálogo modal de confirmación antes de cerrar la sesión activa
    private void mostrarDialogoCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_hu22_cerrar_sesion, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // ESTO SIRVE PARA: Enlazar los botones de cancelar y confirmar dentro del diálogo de cierre
        Button btnCancelar = dialogView.findViewById(R.id.btn_hu22_cancelar);
        Button btnConfirmar = dialogView.findViewById(R.id.btn_hu22_confirmar);

        // ESTO SIRVE PARA: Si el alumno cancela, simplemente cierra el diálogo
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        // ESTO SIRVE PARA: Si el alumno confirma, vacía las SharedPreferences y vuelve a la pantalla de login
        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                dialog.dismiss();
                SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                prefs.edit().clear().apply();

                CustomToast.show(this, "Sesión cerrada correctamente", false);

                // GRACIAS A ESTO: Redirige al login limpiando la pila de actividades para no volver atrás
                Intent intent = new Intent(PerfilCuentaActivity.this, IniciarSesionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        dialog.show();
    }

    // ESTO SIRVE PARA: Regresar al Dashboard principal de descanso
    private void volverAInicio() {
        Intent intent = new Intent(this, ResumenDescansoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
