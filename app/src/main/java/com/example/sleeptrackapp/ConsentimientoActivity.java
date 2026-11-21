package com.example.sleeptrackapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// PANTALLA: Consentimiento informado. Informa sobre la recolección de métricas y solicita autorización para uso de sensores.
public class ConsentimientoActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar la casilla de verificación de consentimiento y el botón para continuar
    CheckBox cbAcepto;
    Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Cargar el layout visual XML con el texto legal de consentimiento
        setContentView(R.layout.activity_hu05_consentimiento_monitoreo);

        // ESTO SIRVE PARA: Configurar márgenes contra las barras de navegación del dispositivo móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar el checkbox de autorización y el botón de avanzar con Java
        cbAcepto = findViewById(R.id.cb_hu03_acepto);
        btnContinuar = findViewById(R.id.btn_hu03_continuar);

        // ESTO SIRVE PARA: Validar la decisión del estudiante y avanzar a la configuración de horas
        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> {
                // ESTO SIRVE PARA: Verificar si el checkbox fue marcado por el usuario
                boolean sensoresAceptados = false;
                if (cbAcepto != null) {
                    sensoresAceptados = cbAcepto.isChecked();
                }

                // ESTO SIRVE PARA: Persistir la decisión de permisos de sensores en el archivo SharedPreferences local
                SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("sensores_aceptados", sensoresAceptados).apply();

                // GRACIAS A ESTO: Navega a la siguiente pantalla para definir el horario objetivo de descanso
                Intent intent = new Intent(ConsentimientoActivity.this, ConfigurarDescansoActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
