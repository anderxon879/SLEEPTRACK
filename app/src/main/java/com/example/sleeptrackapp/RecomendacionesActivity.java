package com.example.sleeptrackapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

// PANTALLA: Recomendaciones inteligentes. Muestra consejos clasificados por chips (pantallas, estrés, cafeína, horarios).
public class RecomendacionesActivity extends AppCompatActivity {

    // ESTO SIRVE PARA: Declarar las tarjetas de consejos personalizados y alertas preventivas
    View ivVolver;
    View cardConsejoPersonalizado, cardAlertaPreventiva, cardLogroSemanal;
    TextView tvConsejoPersTitulo, tvConsejoPersResumen, tvConsejoPersDesc;
    TextView tvAlertaDesc, tvLogroDesc;

    // ESTO SIRVE PARA: Declarar las tarjetas de consejos por cada categoría específica
    View cardRecPantallas1, cardRecPantallas2;
    View cardRecHorarios1, cardRecHorarios2;
    View cardRecEstres1, cardRecEstres2;
    View cardRecCafeina1, cardRecCafeina2;

    // ESTO SIRVE PARA: Declarar los chips de filtro (Todos, Horarios, Pantallas, Estrés, Cafeína)
    TextView chipTodos, chipHorarios, chipPantallas, chipEstres, chipCafeina;
    private TextView[] chips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ESTO SIRVE PARA: Inflar el layout XML de recomendaciones de higiene del sueño
        setContentView(R.layout.activity_hu19_hu20_recomendaciones);

        // ESTO SIRVE PARA: Regresar al Dashboard al presionar la flecha
        ivVolver = findViewById(R.id.iv_hu19_volver);
        if (ivVolver != null) {
            ivVolver.setOnClickListener(v -> volverAInicio());
        }

        // ESTO SIRVE PARA: Capturar el botón de retroceso del teléfono
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                volverAInicio();
            }
        });

        // ESTO SIRVE PARA: Vincular las tarjetas de consejos y alertas con Java
        cardConsejoPersonalizado = findViewById(R.id.card_consejo_personalizado);
        tvConsejoPersTitulo = findViewById(R.id.tv_consejo_pers_titulo);
        tvConsejoPersResumen = findViewById(R.id.tv_consejo_pers_resumen);
        tvConsejoPersDesc = findViewById(R.id.tv_consejo_pers_desc);

        cardAlertaPreventiva = findViewById(R.id.card_alerta_preventiva);
        cardLogroSemanal = findViewById(R.id.card_logro_semanal);
        tvAlertaDesc = findViewById(R.id.tv_hu20_alerta_desc);
        tvLogroDesc = findViewById(R.id.tv_hu20_logro_desc);

        // ESTO SIRVE PARA: Vincular las tarjetas individuales de consejos temáticos
        cardRecPantallas1 = findViewById(R.id.card_rec_pantallas_1);
        cardRecPantallas2 = findViewById(R.id.card_rec_pantallas_2);
        cardRecHorarios1 = findViewById(R.id.card_rec_horarios_1);
        cardRecHorarios2 = findViewById(R.id.card_rec_horarios_2);
        cardRecEstres1 = findViewById(R.id.card_rec_estres_1);
        cardRecEstres2 = findViewById(R.id.card_rec_estres_2);
        cardRecCafeina1 = findViewById(R.id.card_rec_cafeina_1);
        cardRecCafeina2 = findViewById(R.id.card_rec_cafeina_2);

        // ESTO SIRVE PARA: Vincular los botones de chip para filtrar por tema
        chipTodos = findViewById(R.id.chip_cat_todos);
        chipHorarios = findViewById(R.id.chip_cat_horarios);
        chipPantallas = findViewById(R.id.chip_cat_pantallas);
        chipEstres = findViewById(R.id.chip_cat_estres);
        chipCafeina = findViewById(R.id.chip_cat_cafeina);

        chips = new TextView[]{chipTodos, chipHorarios, chipPantallas, chipEstres, chipCafeina};

        // ESTO SIRVE PARA: Configurar los escuchadores de clic en cada chip de categoría
        configurarFiltros();

        // ESTO SIRVE PARA: Adaptar los textos de consejos según el uso de pantallas y calidad del alumno
        adaptarConsejosSegunUso();

        // ESTO SIRVE PARA: Configurar la barra de navegación inferior (pestaña Consejos activa)
        BottomNavHelper.setup(this, R.id.nav_consejos);
    }

    // ESTO SIRVE PARA: Asignar el evento onClick a cada uno de los chips de categoría
    private void configurarFiltros() {
        if (chipTodos != null) chipTodos.setOnClickListener(v -> filtrarCategoria(0));
        if (chipHorarios != null) chipHorarios.setOnClickListener(v -> filtrarCategoria(1));
        if (chipPantallas != null) chipPantallas.setOnClickListener(v -> filtrarCategoria(2));
        if (chipEstres != null) chipEstres.setOnClickListener(v -> filtrarCategoria(3));
        if (chipCafeina != null) chipCafeina.setOnClickListener(v -> filtrarCategoria(4));
    }

    // ESTO SIRVE PARA: Filtrar y cambiar visualmente las tarjetas visibles según la categoría seleccionada
    private void filtrarCategoria(int categoriaIndex) {
        // ESTO SIRVE PARA: Resaltar el chip seleccionado y atenuar los demás chips
        for (int i = 0; i < chips.length; i++) {
            if (chips[i] != null) {
                if (i == categoriaIndex) {
                    chips[i].setBackgroundResource(R.drawable.bg_option_selected);
                    chips[i].setTextColor(getColor(R.color.primary));
                } else {
                    chips[i].setBackgroundResource(R.drawable.bg_option_unselected);
                    chips[i].setTextColor(getColor(R.color.text_muted));
                }
            }
        }

        // ESTO SIRVE PARA: Determinar qué bloques temáticos deben permanecer visibles
        boolean showAll = (categoriaIndex == 0);
        boolean showHorarios = (showAll || categoriaIndex == 1);
        boolean showPantallas = (showAll || categoriaIndex == 2);
        boolean showEstres = (showAll || categoriaIndex == 3);
        boolean showCafeina = (showAll || categoriaIndex == 4);

        // ESTO SIRVE PARA: Actualizar la visibilidad de cada tarjeta temática
        setVis(cardRecHorarios1, showHorarios);
        setVis(cardRecHorarios2, showHorarios);
        setVis(cardRecPantallas1, showPantallas);
        setVis(cardRecPantallas2, showPantallas);
        setVis(cardRecEstres1, showEstres);
        setVis(cardRecEstres2, showEstres);
        setVis(cardRecCafeina1, showCafeina);
        setVis(cardRecCafeina2, showCafeina);

        // ESTO SIRVE PARA: Mostrar u ocultar la tarjeta de consejo personalizado
        if (cardConsejoPersonalizado != null) {
            cardConsejoPersonalizado.setVisibility((showAll || showPantallas) ? View.VISIBLE : View.GONE);
        }
    }

    // ESTO SIRVE PARA: Helper para cambiar la visibilidad de cualquier vista entre VISIBLE y GONE
    private void setVis(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // ESTO SIRVE PARA: Personalizar los consejos y advertencias basándose en las métricas reales del estudiante
    private void adaptarConsejosSegunUso() {
        // ESTO SIRVE PARA: Leer interacciones nocturnas con la pantalla y porcentaje de calidad
        SharedPreferences prefs = getSharedPreferences("SleepTrackPrefs", MODE_PRIVATE);
        int interacciones = prefs.getInt("interacciones_pantalla", 0);
        int calidadPct = prefs.getInt("ultima_calidad_porcentaje", 85);

        // ESTO SIRVE PARA: Mostrar recomendación contra pantallas si el alumno interactuó de noche con el móvil
        if (interacciones > 0) {
            if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("Detectamos " + interacciones + " encendidos de pantalla anoche");
            if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: Desactiva notificaciones y pon el móvil en 'No Molestar' para evitar microdespertares.");
            if (tvConsejoPersDesc != null) tvConsejoPersDesc.setText("Interactuar con el smartphone a mitad de la noche rompe la arquitectura del sueño REM. Pon el teléfono a más de 1 metro de distancia.");
        } else {
            if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("¡Excelente higiene digital nocturna!");
            if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: Mantuviste tu celular sin interacciones nocturnas, protegiendo tu sueño profundo.");
            if (tvConsejoPersDesc != null) tvConsejoPersDesc.setText("Continúa dejando tu móvil lejos de la cama para consolidar tu ciclo de descanso.");
        }

        // ESTO SIRVE PARA: Mostrar tarjeta de alerta preventiva si el cumplimiento fue menor al 65%
        if (calidadPct < 65) {
            if (cardAlertaPreventiva != null) cardAlertaPreventiva.setVisibility(View.VISIBLE);
            if (cardLogroSemanal != null) cardLogroSemanal.setVisibility(View.GONE);
        } else {
            if (cardAlertaPreventiva != null) cardAlertaPreventiva.setVisibility(View.GONE);
            if (cardLogroSemanal != null) cardLogroSemanal.setVisibility(View.VISIBLE);
        }

        // ESTO SIRVE PARA: Sincronizar consejos personalizados desde las respuestas de la última encuesta en MySQL
        String correo = prefs.getString("correo_usuario", "");
        if (correo.isEmpty()) return;

        String url = "http://192.168.3.225/SLEEPTRACKAPP/api/obtener_resumen.php";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("correo", correo);
        } catch (Exception ignored) {}

        // ESTO SIRVE PARA: Petición HTTP asíncrona mediante Volley para consultar factores de estrés y cafeína
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    try {
                        if (response.getString("status").equals("success") && response.has("data")) {
                            JSONObject d = response.getJSONObject("data");
                            String pan = d.optString("pantallas", "");
                            String est = d.optString("estres", "");
                            String caf = d.optString("cafeina", "");

                            // Si reportó alto uso de pantallas o cafeína, ajusta el consejo prioritario
                            if (pan.contains("Más de") || pan.contains("60")) {
                                if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("Alto uso de pantallas antes de dormir");
                                if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: Corta el uso de pantallas 45 min antes de acostarte.");
                            } else if (caf.contains("más de una") || caf.contains("2")) {
                                if (tvConsejoPersTitulo != null) tvConsejoPersTitulo.setText("Consumo notable de cafeína reportado");
                                if (tvConsejoPersResumen != null) tvConsejoPersResumen.setText("⚡ Resumen: No bebas café después de las 3:00 PM.");
                            }
                        }
                    } catch (Exception ignored) {}
                },
                error -> {}
        );
        Volley.newRequestQueue(this).add(request);
    }

    // ESTO SIRVE PARA: Regresar al Dashboard principal de descanso
    private void volverAInicio() {
        android.content.Intent intent = new android.content.Intent(this, ResumenDescansoActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
