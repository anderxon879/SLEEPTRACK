package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Locale;

// PANTALLA: Formulario de registro de nuevos estudiantes. Envía los datos a api/register.php con cifrado de clave en backend.
public class CrearPerfilActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar los campos de entrada de datos (nombre, correo, clave y selectores)
    View ivVolver;
    EditText etNombres, etCorreo, etPassword;
    TextView tvFechaNacSelector, tvUnivSelector, tvCarreraSelector;
    Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Cargar el layout XML del formulario de registro
        setContentView(R.layout.activity_hu02_crear_perfil);

        // ESTO SIRVE PARA: Ajustar márgenes para no solapar con la barra de estado y gestos del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar los inputs y selectores del XML con las referencias en Java
        ivVolver = findViewById(R.id.iv_hu02_volver);
        etNombres = findViewById(R.id.et_hu02_nombres);
        etCorreo = findViewById(R.id.et_hu02_correo);
        etPassword = findViewById(R.id.et_hu02_password);
        tvFechaNacSelector = findViewById(R.id.tv_hu02_fecha_nac_selector);
        tvUnivSelector = findViewById(R.id.tv_hu02_universidad_selector);
        tvCarreraSelector = findViewById(R.id.tv_hu02_carrera_selector);
        btnContinuar = findViewById(R.id.btn_hu02_continuar);

        // ESTO SIRVE PARA: Regresar a la pantalla anterior al presionar la flecha
        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> finish());
        }

        // ESTO SIRVE PARA: Abrir el selector interactivo DatePickerDialog de fecha de nacimiento
        if (tvFechaNacSelector != null) {
            tvFechaNacSelector.setOnClickListener(v -> mostrarDatePicker(tvFechaNacSelector));
        }
        // ESTO SIRVE PARA: Desplegar diálogo modal con la lista de universidades peruanas
        if (tvUnivSelector != null) {
            tvUnivSelector.setOnClickListener(v -> mostrarDialogoSelector(tvUnivSelector, R.array.universidades, "Selecciona tu universidad"));
        }
        // ESTO SIRVE PARA: Desplegar diálogo modal con la lista de carreras universitarias
        if (tvCarreraSelector != null) {
            tvCarreraSelector.setOnClickListener(v -> mostrarDialogoSelector(tvCarreraSelector, R.array.carreras, "Selecciona tu carrera"));
        }

        // ESTO SIRVE PARA: Instanciar SharedPreferences para guardar localmente la sesión tras el registro
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);

        // ESTO SIRVE PARA: Botón de validación y envío de credenciales a la API en PHP
        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> {
                String nombres = etNombres != null ? etNombres.getText().toString().trim() : "";
                String correo = etCorreo != null ? etCorreo.getText().toString().trim() : "";
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";

                // ESTO SIRVE PARA: Validar que los campos obligatorios no se encuentren vacíos
                if (nombres.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                    CustomToast.show(this, "Por favor, completa tus datos principales", true);
                    return;
                }

                String fechaNac = tvFechaNacSelector != null ? tvFechaNacSelector.getText().toString() : "";
                String universidad = tvUnivSelector != null ? tvUnivSelector.getText().toString() : "";
                String carrera = tvCarreraSelector != null ? tvCarreraSelector.getText().toString() : "";

                // ESTO SIRVE PARA: URL del script PHP en XAMPP que registra el estudiante en MySQL
                String url = "http://192.168.3.225/SLEEPTRACKAPP/api/register.php";

                // ESTO SIRVE PARA: Empaquetar los datos del alumno en un objeto JSON
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("nombres", nombres);
                    requestData.put("correo", correo);
                    requestData.put("password", password);
                    requestData.put("fecha_nacimiento", fechaNac);
                    requestData.put("universidad", universidad);
                    requestData.put("carrera", carrera);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                btnContinuar.setEnabled(false);

                // ESTO SIRVE PARA: Enviar la petición HTTP POST asíncrona mediante la librería Volley
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                        response -> {
                            btnContinuar.setEnabled(true);
                            try {
                                String status = response.getString("status");
                                String message = response.getString("message");

                                // GRACIAS A ESTO: Si el registro es exitoso, almacena la información de sesión en el celular
                                if (status.equals("success")) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("nombre_usuario", nombres);
                                    editor.putString("correo_usuario", correo);
                                    editor.putString("fecha_nac_usuario", fechaNac);
                                    editor.putString("universidad_usuario", universidad);
                                    editor.putString("carrera_usuario", carrera);
                                    editor.apply();

                                    CustomToast.show(this, "Perfil creado exitosamente", false);

                                    // Navega al flujo de consentimiento y permisos de sensores
                                    Intent intent = new Intent(CrearPerfilActivity.this, ConsentimientoActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Notifica si el correo ya existe o hubo error de validación
                                    CustomToast.show(this, message, true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                CustomToast.show(this, "Error conectando con el servidor", true);
                            }
                        },
                        error -> {
                            btnContinuar.setEnabled(true);
                            CustomToast.show(this, "Error de red. Revisa tu conexión a internet", true);
                        }
                );

                // Agrega la petición a la cola de procesamiento en red de Volley
                Volley.newRequestQueue(this).add(jsonObjectRequest);
            });
        }
    }

    // ESTO SIRVE PARA: Mostrar un cuadro de diálogo con opciones seleccionables (universidad / carrera)
    private void mostrarDialogoSelector(TextView targetView, int arrayResId, String titulo) {
        String[] opciones = getResources().getStringArray(arrayResId);
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(titulo)
                .setItems(opciones, (dialog, which) -> {
                    targetView.setText(opciones[which]);
                    targetView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                })
                .show();
    }

    // ESTO SIRVE PARA: Desplegar el selector de calendario nativo para registrar la fecha de nacimiento
    private void mostrarDatePicker(TextView targetView) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int anio = cal.get(java.util.Calendar.YEAR) - 20;
        int mes = cal.get(java.util.Calendar.MONTH);
        int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fechaStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    targetView.setText(fechaStr);
                    targetView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }, anio, mes, dia);
        datePickerDialog.show();
    }
}
