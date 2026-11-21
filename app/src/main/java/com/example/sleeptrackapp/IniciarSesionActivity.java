package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.Locale;

// PANTALLA: Inicio de sesión y recuperación de clave. Valida credenciales con BCrypt en api/login.php y persiste la sesión.
public class IniciarSesionActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar los campos de texto para correo, contraseña y botones de acceso
    View btnVolver;
    EditText etCorreo, etPassword;
    Button btnEntrar;
    TextView tvOlvidePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Cargar el layout XML del formulario de login
        setContentView(R.layout.activity_hu03_iniciar_sesion);

        // ESTO SIRVE PARA: Ajustar márgenes para no solaparse con barras de navegación y estado del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar los controles visuales del layout XML con los objetos Java
        btnVolver = findViewById(R.id.btn_login_volver);
        etCorreo = findViewById(R.id.et_login_correo);
        etPassword = findViewById(R.id.et_login_password);
        btnEntrar = findViewById(R.id.btn_login_entrar);
        tvOlvidePass = findViewById(R.id.tv_login_olvide_pass);

        // ESTO SIRVE PARA: Regresar a la pantalla anterior si el usuario presiona la flecha
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // ESTO SIRVE PARA: Botón para validar credenciales y entrar a la aplicación
        if (btnEntrar != null) {
            btnEntrar.setOnClickListener(v -> loginUser());
        }

        // ESTO SIRVE PARA: Abrir el diálogo modal interactivo para restablecer la contraseña olvidada
        if (tvOlvidePass != null) {
            tvOlvidePass.setOnClickListener(v -> mostrarDialogoRecuperarPassword());
        }
    }

    // ESTO SIRVE PARA: Validar credenciales enviando un POST con Volley al backend PHP
    private void loginUser() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // ESTO SIRVE PARA: Comprobar que no haya campos en blanco antes de hacer la llamada a la red
        if (correo.isEmpty() || password.isEmpty()) {
            CustomToast.show(this, "Por favor, completa tus credenciales", true);
            return;
        }

        // ESTO SIRVE PARA: URL del script de login en XAMPP que valida el hash BCrypt en la base de datos
        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/login.php";

        // ESTO SIRVE PARA: Empaquetar correo y clave en formato JSON para el envío
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
            requestData.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ESTO SIRVE PARA: Ejecutar la petición HTTP POST asíncrona mediante la librería Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        // GRACIAS A ESTO: Si las credenciales son válidas, guarda la sesión del usuario en memoria local
                        if (status.equals("success")) {
                            JSONObject user = response.getJSONObject("user");

                            // ESTO SIRVE PARA: Persistir nombre, correo y horarios en SharedPreferences para mantener sesión abierta
                            SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("nombre_usuario", user.getString("nombres"));
                            editor.putString("correo_usuario", correo);

                            // ESTO SIRVE PARA: Restaurar horarios de sueño y metas previas del estudiante si existen
                            if (user.has("hora_dormir") && !user.isNull("hora_dormir") && !user.getString("hora_dormir").isEmpty()) {
                                editor.putString("hora_dormir", user.getString("hora_dormir"));
                                editor.putString("hora_despertar", user.getString("hora_despertar"));

                                String meta_horas = user.getString("meta_horas");
                                editor.putString("meta_horas", meta_horas);
                                editor.putString("duracion_calculada_texto", meta_horas);

                                int[] dur = SleepTimeUtils.calcularHorasYMinutos(user.getString("hora_dormir"), user.getString("hora_despertar"));
                                editor.putInt("duracion_minutos_totales", dur[0] * 60 + dur[1]);
                                editor.putString("duracion_calculada_corto", SleepTimeUtils.formatearDuracionCorto(dur[0], dur[1]));
                            }

                            editor.apply();

                            CustomToast.show(IniciarSesionActivity.this, "¡Bienvenido " + user.getString("nombres") + "!", false);

                            // ESTO SIRVE PARA: Redirigir al Dashboard principal (ResumenDescansoActivity) limpiando la pila anterior
                            Intent intent = new Intent(IniciarSesionActivity.this, ResumenDescansoActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Muestra mensaje de contraseña o correo incorrecto
                            CustomToast.show(IniciarSesionActivity.this, message, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        CustomToast.show(IniciarSesionActivity.this, "Error conectando con el servidor", true);
                    }
                },
                error -> CustomToast.show(IniciarSesionActivity.this, "Error de red. Revisa tu conexión", true)
        );

        // Agrega la solicitud a la cola de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    // ESTO SIRVE PARA: Mostrar el diálogo modal que permite restablecer la contraseña mediante verificación de identidad
    private void mostrarDialogoRecuperarPassword() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_hu04_recuperar_password, null);

        // ESTO SIRVE PARA: Construir el cuadro modal emergente con fondo transparente
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // ESTO SIRVE PARA: Enlazar los campos del diálogo (correo, selector de fecha, nueva clave y botones)
        EditText etCorreoRec = dialogView.findViewById(R.id.et_recuperar_correo);
        TextView tvFechaRec = dialogView.findViewById(R.id.tv_recuperar_fecha);
        EditText etNuevaPass = dialogView.findViewById(R.id.et_recuperar_password);
        Button btnCancelar = dialogView.findViewById(R.id.btn_recuperar_cancelar);
        Button btnConfirmar = dialogView.findViewById(R.id.btn_recuperar_confirmar);

        // ESTO SIRVE PARA: Desplegar selector de fecha de nacimiento para validar que es el titular de la cuenta
        if (tvFechaRec != null) {
            tvFechaRec.setOnClickListener(v -> {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                int anio = cal.get(java.util.Calendar.YEAR) - 20;
                int mes = cal.get(java.util.Calendar.MONTH);
                int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        (view, year, month, dayOfMonth) -> {
                            String fechaStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                            tvFechaRec.setText(fechaStr);
                            tvFechaRec.setTextColor(getColor(R.color.text_primary));
                        }, anio, mes, dia);
                datePickerDialog.show();
            });
        }

        // ESTO SIRVE PARA: Cancelar la recuperación y cerrar el cuadro modal
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        // ESTO SIRVE PARA: Confirmar y enviar la nueva contraseña cifrada a api/recuperar.php
        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                String correo = etCorreoRec.getText().toString().trim();
                String fecha = tvFechaRec.getText().toString().trim();
                String nuevaPass = etNuevaPass.getText().toString().trim();

                // Validación de campos obligatorios en el diálogo
                if (correo.isEmpty() || fecha.equals("DD/MM/AAAA") || nuevaPass.isEmpty()) {
                    CustomToast.show(this, "Completa todos los campos de recuperación", true);
                    return;
                }

                String url = "http://192.168.3.225/SLEEPTRACKAPP/api/recuperar.php";

                // ESTO SIRVE PARA: Empaquetar correo, fecha de nacimiento y nueva clave en JSON
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("correo", correo);
                    requestData.put("fecha_nacimiento", fecha);
                    requestData.put("nueva_password", nuevaPass);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnConfirmar.setEnabled(false);

                // ESTO SIRVE PARA: Enviar la petición POST con Volley para actualizar la contraseña en MySQL
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                        response -> {
                            btnConfirmar.setEnabled(true);
                            try {
                                String status = response.getString("status");
                                String message = response.getString("message");

                                if (status.equals("success")) {
                                    CustomToast.show(this, message, false);
                                    dialog.dismiss();
                                } else {
                                    CustomToast.show(this, message, true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            btnConfirmar.setEnabled(true);
                            CustomToast.show(this, "Error de red al recuperar contraseña", true);
                        }
                );

                Volley.newRequestQueue(this).add(jsonObjectRequest);
            });
        }

        dialog.show();
    }
}
