package com.example.sleeptrackapp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

// PANTALLA: Video tutorial de inducción. Reproduce tutorial_sleeptrack.mp4 con VideoView nativo para capacitar al alumno.
public class VideoTutorialActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar el reproductor de video nativo y los controles de reproducción
    private VideoView videoView;
    private TextView btnSaltar;
    private MaterialButton btnContinuar, btnRepetir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Inflar el layout XML que contiene el contenedor de video
        setContentView(R.layout.activity_video_tutorial);

        // ESTO SIRVE PARA: Ajustar márgenes para evitar solapamientos con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_tutorial), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar los controles visuales del layout XML con los objetos Java
        videoView = findViewById(R.id.vv_tutorial);
        btnSaltar = findViewById(R.id.btn_saltar_tutorial);
        btnContinuar = findViewById(R.id.btn_continuar_tutorial);
        btnRepetir = findViewById(R.id.btn_repetir_tutorial);

        // ESTO SIRVE PARA: Solicitar el foco de audio del sistema operativo para una reproducción de audio nítida
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        // ESTO SIRVE PARA: Localizar y cargar el archivo de video local alojado en la carpeta res/raw/
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_sleeptrack);
        videoView.setVideoURI(videoUri);

        // ESTO SIRVE PARA: Iniciar la reproducción automática tan pronto como el video termine de prepararse
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            mp.setVolume(1.0f, 1.0f);
            videoView.start();
        });

        // ESTO SIRVE PARA: Redirigir automáticamente al registro de perfil cuando finalice el video tutorial
        videoView.setOnCompletionListener(mp -> {
            irACrearPerfil();
        });

        // ESTO SIRVE PARA: Permitir al usuario saltar el video si ya conoce el funcionamiento de la app
        if (btnSaltar != null) {
            btnSaltar.setOnClickListener(v -> irACrearPerfil());
        }

        // ESTO SIRVE PARA: Botón 'Continuar' que permite avanzar al registro inmediatamente
        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> irACrearPerfil());
        }

        // ESTO SIRVE PARA: Botón 'Repetir' para reiniciar el video desde el segundo cero
        if (btnRepetir != null) {
            btnRepetir.setOnClickListener(v -> {
                videoView.seekTo(0);
                videoView.start();
            });
        }
    }

    // ESTO SIRVE PARA: Detener el reproductor de video y abrir la pantalla de creación de perfil (CrearPerfilActivity)
    private void irACrearPerfil() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        Intent intent = new Intent(VideoTutorialActivity.this, CrearPerfilActivity.class);
        startActivity(intent);
        finish();
    }

    // ESTO SIRVE PARA: Pausar el video si el usuario sale temporalmente de la aplicación para no consumir batería
    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    // ESTO SIRVE PARA: Reanudar el video automáticamente cuando el estudiante regresa a la aplicación
    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }
}
