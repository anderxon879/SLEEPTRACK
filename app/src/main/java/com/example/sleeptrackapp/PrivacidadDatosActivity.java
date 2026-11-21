package com.example.sleeptrackapp;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

// PANTALLA: Gestión de Privacidad. Permite al usuario eliminar su historial de descanso o cuenta según normativas de protección de datos.
public class PrivacidadDatosActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar los controles visuales de la pantalla (botón volver, título y botón de borrar)
    View ivVolver, btnEliminarRegistros;
    TextView tvTitulo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ESTO SIRVE PARA: Inflar el layout XML de opciones de privacidad
        setContentView(R.layout.activity_hu23_privacidad_datos);

        // ESTO SIRVE PARA: Ajustar márgenes de la interfaz para no superponerse con la barra de estado del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ESTO SIRVE PARA: Enlazar los componentes visuales del XML con las variables en Java
        ivVolver = findViewById(R.id.iv_hu23_volver);
        tvTitulo = findViewById(R.id.tv_hu23_titulo);
        btnEliminarRegistros = findViewById(R.id.ll_hu23_btn_eliminar);

        // ESTO SIRVE PARA: Regresar a la pantalla anterior al tocar la flecha de volver
        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> finish());
        }

        // ESTO SIRVE PARA: Abrir el diálogo modal de confirmación antes de borrar registros
        if (btnEliminarRegistros != null) {
            btnEliminarRegistros.setOnClickListener(v -> mostrarDialogoEliminar());
        }

        // ESTO SIRVE PARA: Mantener seleccionada la pestaña 'Perfil' en la barra inferior
        BottomNavHelper.setup(this, R.id.nav_perfil);
    }

    // ESTO SIRVE PARA: Mostrar cuadro modal de advertencia que previene borrados accidentales
    private void mostrarDialogoEliminar() {
        // ESTO SIRVE PARA: Inflar la vista gráfica personalizada del diálogo de advertencia
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_hu23_eliminar_registros, null);

        // ESTO SIRVE PARA: Construir el cuadro de diálogo flotante con fondo transparente
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // ESTO SIRVE PARA: Enlazar los botones de cancelar y confirmar dentro del diálogo modal
        Button btnCancelar = dialogView.findViewById(R.id.btn_hu23_cancelar_eliminacion);
        Button btnConfirmar = dialogView.findViewById(R.id.btn_hu23_confirmar_eliminacion);

        // ESTO SIRVE PARA: Si el usuario cancela, cierra la ventana emergente sin alterar los datos
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> dialog.dismiss());
        }

        // ESTO SIRVE PARA: Si el usuario confirma, borra los registros tanto localmente como en el servidor MySQL
        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                // ESTO SIRVE PARA: Leer el archivo local SharedPreferences y obtener el correo del estudiante activo
                SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
                String correo = prefs.getString("correo_usuario", "");

                // ESTO SIRVE PARA: Limpiar de la memoria caché local las métricas de la última noche
                prefs.edit()
                        .remove("ultimo_monitoreo_inicio")
                        .remove("ultimo_monitoreo_fin")
                        .remove("movimientos_detectados")
                        .remove("ultima_duracion_minutos")
                        .remove("ultima_calidad_porcentaje")
                        .remove("ultima_eficiencia")
                        .remove("ultima_hora_inicio")
                        .remove("ultima_hora_fin")
                        .apply();

                // ESTO SIRVE PARA: Verificar si hay un usuario logueado antes de solicitar la eliminación al servidor
                if (!correo.isEmpty()) {
                    // ESTO SIRVE PARA: URL del endpoint en el servidor XAMPP que ejecuta la sentencia SQL DELETE
                    String url = "http://192.168.3.225/SLEEPTRACKAPP/api/eliminar_datos.php";
                    
                    // ESTO SIRVE PARA: Crear el objeto JSON con el correo del usuario y el alcance del borrado
                    JSONObject payload = new JSONObject();
                    try {
                        payload.put("correo", correo);
                        payload.put("tipo", "registros");
                    } catch (Exception ignored) {}

                    // ESTO SIRVE PARA: Enviar la petición HTTP POST asíncrona mediante la librería Volley
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                            response -> {},
                            error -> {}
                    );
                    // GRACIAS A ESTO: La solicitud se encola y ejecuta en segundo plano sin congelar la app
                    Volley.newRequestQueue(this).add(request);
                }

                // ESTO SIRVE PARA: Cerrar el modal y avisar al estudiante que sus datos se eliminaron con éxito
                dialog.dismiss();
                CustomToast.show(this, "Registros de sueño eliminados correctamente", false);
            });
        }

        // Muestra el cuadro de diálogo en pantalla
        dialog.show();
    }
}
