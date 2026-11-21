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

// PANTALLA: Bienvenida inicial. Muestra el acceso principal y detecta si ya hay una sesión activa guardada.
public class BienvenidaActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar los componentes gráficos de textos y botones de la pantalla inicial
    TextView tvAppName, tvTitulo, tvDesc, tvComenzar;
    Button btnYaTengoCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ESTO SIRVE PARA: Verificar si el usuario ya inició sesión previamente y evitar pedir credenciales otra vez
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        String correoGuardado = prefs.getString("correo_usuario", "");
        if (!correoGuardado.isEmpty()) {
            // GRACIAS A ESTO: Si existe sesión activa, redirige automáticamente al Dashboard principal sin demoras
            Intent intent = new Intent(BienvenidaActivity.this, ResumenDescansoActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // ESTO SIRVE PARA: Habilitar diseño de pantalla completa de borde a borde
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Cargar el layout visual XML correspondiente a la bienvenida
        setContentView(R.layout.activity_hu01_bienvenida);

        // ESTO SIRVE PARA: Ajustar márgenes para evitar solapamientos con la barra de estado y gestos de navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar los identificadores del XML con las referencias en Java
        tvAppName = findViewById(R.id.tv_hu01_app_name);
        tvTitulo = findViewById(R.id.tv_hu01_titulo);
        tvDesc = findViewById(R.id.tv_hu01_desc);
        tvComenzar = findViewById(R.id.tv_hu01_comenzar);
        btnYaTengoCuenta = findViewById(R.id.btn_hu01_ya_tengo_cuenta);

        // ESTO SIRVE PARA: Botón 'Comenzar' que abre el video tutorial interactivo antes del registro
        if (tvComenzar != null) {
            tvComenzar.setOnClickListener(v -> {
                Intent intent = new Intent(BienvenidaActivity.this, VideoTutorialActivity.class);
                startActivity(intent);
            });
        }

        // ESTO SIRVE PARA: Botón 'Ya tengo cuenta' que abre directamente la pantalla de inicio de sesión
        if (btnYaTengoCuenta != null) {
            btnYaTengoCuenta.setOnClickListener(v -> {
                Intent intent = new Intent(BienvenidaActivity.this, IniciarSesionActivity.class);
                startActivity(intent);
            });
        }
    }
}
